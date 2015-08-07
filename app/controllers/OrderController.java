package controllers;

import com.mongodb.DBObject;
import com.mongodb.BasicDBList;

import models.*;
import models.Collection;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import play.*;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.mvc.*;

import services.MongoService;
import views.html.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.Logger;
import java.text.DecimalFormat;

public class OrderController extends Controller {


	 @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result order(String id){
        Order order = MongoService.findOrderById(id);
        if(order!=null){
            return ok(detailOrder.render(order));
        }else if(id!=null&&order==null){
            return redirect(routes.OrderController.listOrders());
        }else{
            order = new Order();
            List<User> users = MongoService.getAllUsers();
            List<Inventory> inventories = MongoService.getAllInventories();
            List<GiftCard> giftCards = MongoService.getAllGiftCards();
            List<DiscountCode> discountCodes = MongoService.getAllDiscountCodes();
            return ok(newOrder.render(order,inventories,users,discountCodes,giftCards));
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result listOrders() {
        List<Order> orders = MongoService.getAllOrders();
        return ok(listOrders.render(orders));
    }

      @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveOrder(String id){

        //Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String[] productIds = (dataFiles.get("product") != null && dataFiles.get("product").length > 0) ? dataFiles.get("product")  : null;
        String[] quantities = (dataFiles.get("quantity") != null && dataFiles.get("quantity").length > 0) ? dataFiles.get("quantity") : null;
        String userId = (dataFiles.get("user") != null && dataFiles.get("user").length > 0) ? dataFiles.get("user")[0] : null;
        String email = (dataFiles.get("email") != null && dataFiles.get("email").length > 0) ? dataFiles.get("email")[0] : null;
        String giftCardId = (dataFiles.get("giftCard") != null && dataFiles.get("giftCard").length > 0) ? dataFiles.get("giftCard")[0] : null;
        String discountCodeId = (dataFiles.get("discountCode") != null && dataFiles.get("discountCode").length > 0) ? dataFiles.get("discountCode")[0] : null;
        String statusCode = (dataFiles.get("status") != null && dataFiles.get("status").length > 0) ? dataFiles.get("status")[0] : null;


        User user = null;
        GiftCard giftCard = null;
        DiscountCode discountCode = null;
        Utils.StatusCompra status = Utils.StatusCompra.getStatusByCode(Integer.parseInt(statusCode));
        List<Inventory> inventories = new ArrayList<>();

        if(userId!=null&&!userId.equals("")){
            user = MongoService.findUserById(userId);
        }
        if(giftCardId!=null&&!giftCardId.equals("")){
            giftCard = MongoService.findGiftCardById(giftCardId);
        }
        if(discountCodeId!=null&&!discountCodeId.equals("")){
            discountCode = MongoService.findDiscountCodeById(discountCodeId);
        }

        //build inventory object
        Order order = new Order();

        //save order already in base
        if(id!=null&&!id.equals("")&&MongoService.hasOrderById(id)) {
            order = MongoService.findOrderById(id);
            StatusOrder statusOrder = new StatusOrder();
            statusOrder.setStatus(status);
            order.getStatus().add(statusOrder);
            MongoService.saveOrder(order);
            return redirect(routes.OrderController.listOrders());
        }

        //Validation

        if(id!=null&&!id.equals("")&&!MongoService.hasOrderById(id)) {
            flash("listOrders","Order not in base");
            return redirect(routes.OrderController.listOrders());
        }

        if(productIds==null||(productIds!=null&&productIds.length<=0)){
            flash("order","Insert A product");
            return redirect(routes.OrderController.order(null));
        }

        if(email==null||(email!=null&&email.equals(""))){
            flash("order","Insert a user or email");
            return redirect(routes.OrderController.order(null));
        }
        if(user==null&&(email==null||(email!=null&&email.equals("")))){
            flash("order","Insert a user or email");
            return redirect(routes.OrderController.order(null));   
        }

  
      
        //validation and inventories changes on quantity
        if(productIds.length>0){
            for(int i=0;i<productIds.length;i++){
                try{
                    Inventory inventory = MongoService.findInventoryById(productIds[i]);
                    int quantity =  Integer.parseInt(quantities[i].trim());
                    if(quantity<=0||inventory.getQuantity()<quantity){
                        flash("order","Inventory without quantity or quantity <= 0");
                        return redirect(routes.OrderController.order(null));
                    }else{
                        inventory.setQuantity(inventory.getQuantity() -quantity );
                        inventories.add(inventory);

                        Inventory inventoryOrder = new Inventory(inventory);
                        inventoryOrder.setQuantity(quantity);
                        
                        order.getProducts().add(inventoryOrder);

                    }
                }catch(Exception e){
                    flash("order","Inventory error "+e.toString());
                    return redirect(routes.OrderController.order(null));
                }
            }
        }


        double total = OrderController.calculateFinalPrice(inventories,discountCode,giftCard);

        

        order.setEmail(email);
        order.setUser(user);
        order.setTotal(total);
        order.setGiftCard(giftCard);
        order.setDiscountCode(discountCode);

        StatusOrder statusOrder = new StatusOrder();
        statusOrder.setStatus(status);
        order.getStatus().add(statusOrder);


        //save Inventory change
        MongoService.saveInventories(inventories);

        MongoService.saveOrder(order);

        return redirect(routes.OrderController.listOrders());
    } 


     @Security.Authenticated(Secured.class)
    public static Result getDiscounteCodeApplicable(String sku){
        Product product = MongoService.findInventoryById(sku).getProduct();
        List<DiscountCode> discountCodes = MongoService.findDiscountCodeByProduct(product);
        JsonNode json = Json.toJson(discountCodes);
        return ok(json);
    }

     @Security.Authenticated(Secured.class)
    public static Result calculatePrice(){
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();
        String[] productIdsAndQuantity = (dataFiles.get("product[]") != null && dataFiles.get("product[]").length > 0) ? dataFiles.get("product[]")  : null;
        String discountCodeId = (dataFiles.get("discountCodeId") != null && dataFiles.get("discountCodeId").length > 0) ? dataFiles.get("discountCodeId")[0] : null;
        List<String> productIds =  new ArrayList<>();
        for(int i = 0;i<productIdsAndQuantity.length;i++){
                productIds.add(productIdsAndQuantity[i].split("-")[0]);
        }
        List<String> productIdsAndQuantityList = Arrays.asList(productIdsAndQuantity);

        DiscountCode discountCode = null;
        List<Inventory> inventories = new ArrayList<>();

        if(discountCodeId!=null&&!discountCodeId.equals("")){
            discountCode = MongoService.findDiscountCodeById(discountCodeId);
        }
        if(productIds!=null&& productIds.size() >0) {
            inventories = MongoService.findInventoriesByIds(productIds);
        }
        if(inventories.size()>0){
            for(int i=0;i<inventories.size();i++){
                try{
                    final Inventory inventory = inventories.get(i);
                    int quantity =  Integer.parseInt(productIdsAndQuantityList.stream().filter(product -> product.contains(inventory.getSku())).findFirst().get().split("-")[1]);
                    if(quantity<=0||inventory.getQuantity()<quantity){
                        Logger.debug("quantity = "+quantity);
                        Logger.debug("inventories quantity = "+inventory.getQuantity());
                        Logger.debug("inventories sku = "+inventory.getSku());

                        return internalServerError("Out of quantity");
                    }else{
                        inventories.get(i).setQuantity(quantity);
                    }
                }catch(Exception e){

                    return internalServerError("error");
                }
            }
        }
        double total = OrderController.calculateFinalPrice(inventories,discountCode,null);
        DecimalFormat format = new DecimalFormat("0.00");
        String formatted = format.format(total);
        return ok(Json.toJson(formatted));
    }

    public static double calculateFinalPrice(List<Inventory> inventories,DiscountCode discountCode, GiftCard giftCard){
        double total = inventories.stream().mapToDouble(i->i.getProduct().getPrice()*i.getQuantity()).sum();
 
        if(discountCode!=null){

            switch(discountCode.getWhereApply()){
                case oncePerOrder:
                    if(discountIsApplicable(inventories,discountCode)){
                        switch(discountCode.getTypeForPay()){
                            case value:
                                total = total-discountCode.getValueOf();
                            break;
                            case percent:
                                total = total*(1-(discountCode.getValueOf()/100));
                            break;
                        }
                    }
                    break;
                case toEveryProduct:
                    total = 0;
                    for(Inventory inventory: inventories){
                        double value = inventory.getProduct().getPrice()*inventory.getQuantity();
                        List<Inventory> newListInverntory = new ArrayList<>();
                        newListInverntory.add(inventory);
                        if(discountIsApplicable(newListInverntory,discountCode)){
                            switch(discountCode.getTypeForPay()){
                                case value:
                                    value = value-discountCode.getValueOf();
                                break;
                                case percent:
                                    value = value*(1-(discountCode.getValueOf()/100));
                                break;
                            }
                        }
                        total += value;
                    }
                    
                    break;
            }
        }
        return total;
    }

    public static boolean discountIsApplicable(List<Inventory> inventories,DiscountCode discountCode){
        double total = inventories.stream().mapToDouble(i->i.getProduct().getPrice()*i.getQuantity()).sum();
        Set<String> colelctionsSlug = new HashSet<>();
        Set<String> productsSlug = new HashSet<>();
        boolean collectionActive = false;
        boolean productActive = false;

        for(Inventory i:inventories){
            colelctionsSlug.addAll(i.getProduct().getCollectionsSlugs());
            productsSlug.add(i.getProduct().getSlug());
        }

        if(discountCode.getCollectionsSlug()!=null){

            for(String collectionsSlug : discountCode.getCollectionsSlug()){
                if(colelctionsSlug.contains(collectionsSlug)){
                    collectionActive = true;
                }
            }

        }

        if(discountCode.getProductSlugs()!=null){
            for(String productSlug : discountCode.getProductSlugs()){
                if(productsSlug.contains(productSlug)){
                    productActive = true;
                }
            }
        }

        boolean applicable = false;
        if(discountCode.isActive()){
            if(discountCode.stillOnDate()||discountCode.isNoDateLimits()){
                if(discountCode.isNoTimesLimits()||discountCode.getTimesLeft()>0){
                    switch (discountCode.getOrdersValidation()) {
                        case all:
                            applicable = true;
                            break;
                        case overValue:
                            applicable = (discountCode.getOverValueOf()<=total);
                            break;
                        case collections:
                            applicable =  collectionActive;
                            break;
                        case specificProduct:
                            applicable = productActive;
                            break;
                    }
                }
            }
        }
        return applicable;
    }
    

}
