package controllers;

import akka.actor.ActorRef;
import akka.actor.Props;
import bootstrap.MailSenderActor;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;

import models.*;
import models.Collection;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import play.*;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.libs.Akka;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.*;

import services.MongoService;
import services.SendEmail;
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
    public static Result seeEmailToSend(String id){
        Order order = MongoService.findOrderById(id);
        if(order==null){
            return notFound();
        }else{
            StatusOrder lastSatus = order.getLastStatus();
            String unsub = routes.CostumerController.unsubscribe(order.getEmail()).absoluteURL(request());

            return ok(EmailTemplateOrderStatus.render(order,lastSatus,unsub));
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result listOrders() {
        String statusCompra = request().getQueryString("statusCompra");
        String statusEntrega = request().getQueryString("statusEntrega");
        Logger.debug(statusCompra+"-"+statusEntrega);
        List<Order> orders = null;
        if((statusCompra!=null&&!statusCompra.equals(""))||
            (statusEntrega!=null&&!statusEntrega.equals(""))){
            orders = MongoService.findOrderByStatus(statusCompra,statusEntrega);
        }else{
            orders = MongoService.getAllOrders();
        }
        return ok(listOrders.render(orders,statusEntrega,statusCompra));
    }

    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveOrder(){
        //Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String[] productIds = (dataFiles.get("product") != null && dataFiles.get("product").length > 0) ? dataFiles.get("product")  : null;
        String[] quantities = (dataFiles.get("quantity") != null && dataFiles.get("quantity").length > 0) ? dataFiles.get("quantity") : null;
        String userId = (dataFiles.get("user") != null && dataFiles.get("user").length > 0) ? dataFiles.get("user")[0] : null;
        String email = (dataFiles.get("email") != null && dataFiles.get("email").length > 0) ? dataFiles.get("email")[0] : null;
        String giftCardId = (dataFiles.get("giftCard") != null && dataFiles.get("giftCard").length > 0) ? dataFiles.get("giftCard")[0] : null;
        String discountCodeId = (dataFiles.get("discountCode") != null && dataFiles.get("discountCode").length > 0) ? dataFiles.get("discountCode")[0] : null;
        String statusCode = (dataFiles.get("status") != null && dataFiles.get("status").length > 0) ? dataFiles.get("status")[0] : null;
        String shippingCost = (dataFiles.get("shippingCost") != null && dataFiles.get("shippingCost").length > 0) ? dataFiles.get("shippingCost")[0] : null;
        String notes = (dataFiles.get("notes") != null && dataFiles.get("notes").length > 0) ? dataFiles.get("notes")[0] : null;
        String manualDiscount = (dataFiles.get("manualDiscount") != null && dataFiles.get("manualDiscount").length > 0) ? dataFiles.get("manualDiscount")[0] : null;
        String statusEntregaCode = (dataFiles.get("statusEntrega") != null && dataFiles.get("statusEntrega").length > 0) ? dataFiles.get("statusEntrega")[0] : null;


        User user = null;
        GiftCard giftCard = null;
        DiscountCode discountCode = null;
        Utils.StatusCompra status = Utils.StatusCompra.getStatusByCode(Integer.parseInt(statusCode.equals("")?"0":statusCode));
        Utils.StatusEntrega statusEntrega = Utils.StatusEntrega.getStatusByCode(Integer.parseInt(statusEntregaCode.equals("")?"0":statusEntregaCode));

        List<Inventory> inventories = new ArrayList<>();
        List<InventoryEntry> inventoryEntries = new ArrayList<>();
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



        //Validation
        if(status==null) {
            flash("order","Coloque um status na Venda");
            return redirect(routes.OrderController.order(null));
        }

        if(productIds==null||(productIds!=null&&productIds.length<=0)){
            flash("order","Insert A product");
            return redirect(routes.OrderController.order(null));
        }

        if(user==null&&email!=null&&email.equals("")){
            flash("order","Insert a user or email");
            return redirect(routes.OrderController.order(null));
        }



        //validation and inventories changes on quantity
        if(productIds.length>0){
            for(int i=0;i<productIds.length;i++){
                try{
                    Inventory inventory = MongoService.findInventoryById(productIds[i]);
                    int quantity =  Integer.parseInt(quantities[i].trim());
                    if(quantity<=0||inventory.getQuantity()<quantity&&!inventory.isSellInOutOfStock()){
                        flash("order","Inventory without quantity or quantity <= 0  or set to sellInOutOfStock");
                        return redirect(routes.OrderController.order(null));
                    }else{

                        Order.InventoryOrder inventoryOrder = order.new InventoryOrder();
                        inventoryOrder.cloneInventory(inventory);
                        inventoryOrder.setQuantity(quantity);
                        inventoryOrder.setPriceWithQuantity(inventoryOrder.getProduct().getPrice()*quantity);
                        order.getProducts().add(inventoryOrder);

                    }
                }catch(Exception e){
                    flash("order","Inventory error "+e.toString());
                    return redirect(routes.OrderController.order(null));
                }
            }
        }
        double shippingValue = 0 ;
        double giftCardValue = 0 ;
        double manualDiscountValue = 0;
        if(shippingCost!=null&&!shippingCost.equals(""))
            shippingValue = Double.parseDouble(shippingCost);
        if(manualDiscount!=null&&!manualDiscount.equals(""))
            manualDiscountValue = Double.parseDouble(manualDiscount);

        if(giftCard!=null)
            giftCardValue = giftCard.getPrice();
        
        double totalWithDiscount = OrderController.calculateFinalPrice(order.getProducts(),discountCode);
        double totalValueItems = OrderController.calculateValueItems(order.getProducts());
        double discountValue = (discountCode==null)?(-1*manualDiscountValue):(totalWithDiscount - totalValueItems);
        double total = totalValueItems + shippingValue + discountValue;

        order.setEmail(email!=null&&!email.equals("")?email:user.getEmail());
        order.setUser(user);
        order.setTotalShipping(shippingValue);
        order.setTotalValueItems(totalValueItems);
        order.setGiftCardValue(giftCardValue);
        order.setTotalDiscount(discountValue);
        order.setTotal(total);
        order.setGiftCard(giftCard);
        order.setDiscountCode(discountCode);
        order.setNotes(notes);
        order.setFriendlyId("pos-ordem-"+(MongoService.countAllOrders()+1));

        StatusOrder statusOrder = new StatusOrder();
        statusOrder.setStatus(status);

        order.addStatus(statusOrder);
        order.setStatusEntrega(statusEntrega.name());

        //save Inventory change
        MongoService.saveOrder(order);
        updateInventoryAndDiscountCodeWithStatus(statusOrder,order);

        return redirect(routes.OrderController.listOrders());
    }

    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result updateOrderDetails(String id){
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        //build inventory object
        Order order = new Order();
        String statusCode = (dataFiles.get("status") != null && dataFiles.get("status").length > 0) ? dataFiles.get("status")[0] : null;
        String notes = (dataFiles.get("notes") != null && dataFiles.get("notes").length > 0) ? dataFiles.get("notes")[0] : null;
        String statusEntregaCode = (dataFiles.get("statusEntrega") != null && dataFiles.get("statusEntrega").length > 0) ? dataFiles.get("statusEntrega")[0] : null;
        
        Utils.StatusCompra status = Utils.StatusCompra.getStatusByCode(Integer.parseInt((statusCode!=null)&&!statusCode.equals("")?statusCode:"0"));
        Utils.StatusEntrega statusEntrega = Utils.StatusEntrega.getStatusByCode(Integer.parseInt(statusEntregaCode.equals("")?"0":statusEntregaCode));

        //save order already in base
        if(id!=null&&!id.equals("")&&MongoService.hasOrderById(id)) {
            order = MongoService.findOrderById(id);
            StatusOrder newStatus = null;
            if(status!=null&&order.getLastStatus()!=null&&!order.getLastStatus().getStatus().equals(status)){
                newStatus = new StatusOrder();
                newStatus.setStatus(status);

                updateInventoryAndDiscountCodeWithStatus(newStatus,order);
            }

            MongoService.upDateOrder(order.getId(),newStatus,statusEntrega,notes);

            return redirect(routes.OrderController.order(id));
        }else{
            flash("listOrders","Venda não encontrada");
            return redirect(routes.OrderController.listOrders());
        }
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
        List<Order.InventoryOrder> inventories = new ArrayList<>();

        if(discountCodeId!=null&&!discountCodeId.equals("")){
            discountCode = MongoService.findDiscountCodeById(discountCodeId);
        }
        if(productIds!=null&& productIds.size() >0) {
            Order order = new Order();
            for(Inventory inventory:MongoService.findInventoriesByIds(productIds)){
               Order.InventoryOrder inventoryOrder = order.new InventoryOrder();
                inventoryOrder.cloneInventory(inventory);
                inventories.add(inventoryOrder);
            }
        }
        if(inventories.size()>0){
            for(int i=0;i<inventories.size();i++){
                try{
                    final Order.InventoryOrder inventory = inventories.get(i);
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
        double totalItens = OrderController.calculateValueItems(inventories);
        double total = OrderController.calculateFinalPrice(inventories,discountCode);
        double totalDiscount = total - totalItens;

        DecimalFormat format = new DecimalFormat("0.00");
        String formattedTotal = format.format(total);
        String formattedDiscount = format.format(totalDiscount);
        
        return ok(Json.newObject().put("total",formattedTotal).put("totalDiscount",formattedDiscount));
    }

    public static double calculateValueItems(List<Order.InventoryOrder> inventories){
        double total = inventories.stream().mapToDouble(i->i.getProduct().getPrice()*i.getQuantity()).sum();
        return total;
    }
    public static double calculateFinalPrice(List<Order.InventoryOrder> inventories,DiscountCode discountCode){
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
                    for(Order.InventoryOrder inventory: inventories){
                        double value = inventory.getProduct().getPrice()*inventory.getQuantity();
                        List<Order.InventoryOrder> newListInverntory = new ArrayList<>();
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

    public static boolean discountIsApplicable(List<Order.InventoryOrder> inventories,DiscountCode discountCode){
        double total = inventories.stream().mapToDouble(i->i.getProduct().getPrice()*i.getQuantity()).sum();
        Set<String> colelctionsSlug = new HashSet<>();
        Set<String> productsSlug = new HashSet<>();
        boolean collectionActive = false;
        boolean productActive = false;

        for(Order.InventoryOrder i:inventories){
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

    @Security.Authenticated(Secured.class)
    public static Result sendEmailWithCurrentStatus(String orderId){
        sendEmailWIthStatusOrder(orderId);
        return ok("true");
    }
    @Security.Authenticated(Secured.class)
    public static Result updateOrders(String all){
        try{
            if(all!=null&&!all.equals(""))
                updateAllOrders(all != null && !all.equals(""));
            else{
                return internalServerError();
            }
        }catch(Exception e){
            return internalServerError();
        }
        return ok("true");
    }
    @Security.Authenticated(Secured.class)
    public static Result updateOrder(String orderId){
        try{
            if(orderId!=null&&!orderId.equals(""))
                updateSingleOrder(orderId);
            else{
                return internalServerError();
            }
        }catch(Exception e){
            return internalServerError();
        }
        return ok("true");
    }

    public static Result updatecompra()  {
        try{
            if(request().hasHeader("Origin")&&!request().getHeader("Origin").contains("pagseguro")){
                Logger.debug("hit");
                return badRequest();
            }
            Map<String, String[]> data = request().body().asFormUrlEncoded();
            if(data!=null){

                String urlWs = Play.application().configuration().getString("pagseguro.ws.notification.url");
                String emailPagseguro = null;
                String token = null;
                emailPagseguro =  Play.application().configuration().getString("pagseguro.email");
                token =  Play.application().configuration().getString("pagseguro.token");

                String notificationCode = (data.get("notificationCode") != null && data.get("notificationCode").length > 0) ? data.get("notificationCode")[0] : "";

                String notificationType = (data.get("notificationType") != null && data.get("notificationType").length > 0) ? data.get("notificationType")[0] : "";

                WSRequestHolder holder = WS.url(urlWs + notificationCode);

                WSResponse response =   holder.setQueryParameter("email", emailPagseguro)
                        .setQueryParameter("token", token)
                        .get()
                        .get(10000);

                Document respostaDoc = response.asXml();
                Order order = bindXMLWithPagseguroObject(null,respostaDoc);
                NodeList referenceTag = respostaDoc.getElementsByTagName("reference");
                NodeList statusTag = respostaDoc.getElementsByTagName("status");
                String reference = referenceTag.item(0).getTextContent();
                String status = statusTag.item(0).getTextContent();
                if(order!=null){

                    StatusOrder newStatus = new StatusOrder(Utils.StatusCompra.getStatusByCode(Integer.parseInt(status)));
                   
                    MongoService.upDateOrder(reference,newStatus);

                    if(newStatus.getStatus().equals(Utils.StatusCompra.PAGO)){
                        Utils.StatusEntrega entrega = Utils.StatusEntrega.PRODUCAO;
                        MongoService.upDateOrder(reference,entrega);
                    }else{
                        Utils.StatusEntrega entrega = Utils.StatusEntrega.SEMSTATUS;
                        MongoService.upDateOrder(reference,entrega);
                    }

                    updateInventoryAndDiscountCodeWithStatus(newStatus,order);

                    Order newOrder = MongoService.findOrderById(reference.toString());
                    if(newOrder!=null){
                        String unsub = routes.CostumerController.unsubscribe(newOrder.getEmail()).absoluteURL(request());

                        SendEmail.sendOrderStatus(newOrder,unsub);
                    }
                }else{
                    return notFound();
                }

            }
        }catch(Exception e){
            return internalServerError();
        }
        return ok();
    }

    public static void updateAllOrders(boolean all) {
        List<Order> orders;
        if (!all) {
            List<Order>  ordersAll = MongoService.getAllOrders();
            orders = ordersAll.stream().filter(o -> o.getLastStatus()!=null&&!o.getLastStatus().getStatus().equals(Utils.StatusCompra.PAGO))
                    .collect(Collectors.toList());
        } else {
            orders  = MongoService.getAllOrders();
        }

        for(Order order: orders){
            updateSingleOrder(order.getId());
        }


    }

    public static void updateSingleOrder(String orderId) {
        Order order = MongoService.findOrderById(orderId);

        String urlWs = Play.application().configuration().getString("pagseguro.ws.consult.url");
        String emailPagseguro = null;
        String token = null;
        emailPagseguro = Play.application().configuration().getString("pagseguro.email");
        token = Play.application().configuration().getString("pagseguro.token");
        if (order != null) {

            String code = null;
            if (order.getPagSeguroInfo()==null||order.getPagSeguroInfo().getCode() == null) {
                WSRequestHolder holder = WS.url(urlWs);

                WSResponse responseWithReference = holder.setQueryParameter("email", emailPagseguro)
                        .setQueryParameter("token", token)
                        .setQueryParameter("reference", order.getId())
                        .get()
                        .get(10000);

                if (responseWithReference.getStatus() == 200) {
                    Document respostaReferenceDoc = responseWithReference.asXml();
                    if (respostaReferenceDoc.getElementsByTagName("code").getLength() > 0) {
                        code = respostaReferenceDoc.getElementsByTagName("code").item(0).getTextContent();
                    }
                }
            } else {
                code = order.getPagSeguroInfo().getCode();
            }

            WSRequestHolder holder = WS.url(urlWs + code);

            WSResponse response = holder.setQueryParameter("email", emailPagseguro)
                    .setQueryParameter("token", token)
                    .get()
                    .get(10000);
            if (response.getStatus() == 200) {
                Document respostaDoc = response.asXml();
                if(order.getPagSeguroInfo().getCode() == null){
                    bindXMLWithPagseguroObject(order, respostaDoc);
                }
                NodeList statusTag = respostaDoc.getElementsByTagName("status");
                String status = statusTag.item(0).getTextContent();
                StatusOrder newStatus = new StatusOrder(Utils.StatusCompra.getStatusByCode(Integer.parseInt(status)));
                if (order.getLastStatus() == null ||
                        order.getLastStatus() != null &&
                                !newStatus.getStatus().equals(order.getLastStatus().getStatus())) {

                    MongoService.upDateOrder(order.getId(), newStatus);
                    updateInventoryAndDiscountCodeWithStatus(newStatus, order);
                }
            }
        }
    }


    public static void sendEmailWIthStatusOrder(String orderId) {
        if(orderId!=null&&!orderId.equals("")){
            // send email if PAGO ou CANCELADO
//            ActorRef myActor = Akka.system().actorOf(Props.create(MailSenderActor.class), "myactor");
//            myActor.tell(orderId,null);
            Order order = MongoService.findOrderById(orderId.toString());
            if(order!=null){
                String unsub = routes.CostumerController.unsubscribe(order.getEmail()).absoluteURL(request());

                SendEmail.sendOrderStatus(order,unsub);
            }
        }
    }

    public static Order bindXMLWithPagseguroObject(Order order,Document respostaDoc){
        NodeList dateTag = respostaDoc.getElementsByTagName("date");
        NodeList codeTag = respostaDoc.getElementsByTagName("code");
        NodeList referenceTag = respostaDoc.getElementsByTagName("reference");
        NodeList typeTag = respostaDoc.getElementsByTagName("type");
        NodeList statusTag = respostaDoc.getElementsByTagName("status");
        NodeList cancellationSourceTag = respostaDoc.getElementsByTagName("cancellationSource");
        NodeList lastEventDateTag = respostaDoc.getElementsByTagName("lastEventDate");
        NodeList paymentMethodTag = respostaDoc.getElementsByTagName("paymentMethod");


        NodeList grossAmountTag = respostaDoc.getElementsByTagName("grossAmount");
        NodeList discountAmountTag = respostaDoc.getElementsByTagName("discountAmount");
        NodeList netAmountTag = respostaDoc.getElementsByTagName("netAmount");
        NodeList escrowEndDateTag = respostaDoc.getElementsByTagName("escrowEndDate");
        NodeList extraAmountTag = respostaDoc.getElementsByTagName("extraAmount");
        NodeList installmentCountTag = respostaDoc.getElementsByTagName("installmentCount");
        NodeList creditorFeesTag = respostaDoc.getElementsByTagName("creditorFees");
        NodeList installmentFeeAmountTag = respostaDoc.getElementsByTagName("installmentFeeAmount");
        NodeList operationalFeeAmountTag = respostaDoc.getElementsByTagName("operationalFeeAmount");
        NodeList intermediationRateAmountTag = respostaDoc.getElementsByTagName("intermediationRateAmount");
        NodeList intermediationFeeAmountTag = respostaDoc.getElementsByTagName("intermediationFeeAmount");

        NodeList senderTag = respostaDoc.getElementsByTagName("sender");

        String dateString = dateTag.item(0).getTextContent();
        DateTime date = new DateTime(dateString);


        String code = codeTag.item(0).getTextContent();
        String reference = referenceTag.item(0).getTextContent();
        String type = typeTag.item(0).getTextContent();
        String status = statusTag.item(0).getTextContent();

        String cancellationSource = cancellationSourceTag.getLength()>0?cancellationSourceTag.item(0).getTextContent():"";
        String lastEventDateString = lastEventDateTag.getLength()>0?lastEventDateTag.item(0).getTextContent():"";
        DateTime lastEventDate = (!lastEventDateString.equals(""))?new DateTime(lastEventDateString):null;

        String paymentMethodTag_type =  paymentMethodTag.getLength()>0? paymentMethodTag.item(0).getFirstChild().getTextContent():"";
        String paymentMethodTag_code =  paymentMethodTag.getLength()>0? paymentMethodTag.item(0).getLastChild().getTextContent():"";

        String grossAmount = grossAmountTag.getLength()>0?grossAmountTag.item(0).getTextContent():"";
        String discountAmount = discountAmountTag.getLength()>0?discountAmountTag.item(0).getTextContent():"";
        String netAmount = netAmountTag.getLength()>0?netAmountTag.item(0).getTextContent():"";
        String escrowEndDateString = escrowEndDateTag.getLength()>0?escrowEndDateTag.item(0).getTextContent():"";

        DateTime escrowEndDate = (!escrowEndDateString.equals(""))?new DateTime(escrowEndDateString):null;

        String extraAmount = extraAmountTag.getLength()>0?extraAmountTag.item(0).getTextContent():"";
        String installmentCount = installmentCountTag.getLength()>0?installmentCountTag.item(0).getTextContent():"";
        String installmentFeeAmount = installmentFeeAmountTag.getLength()>0?installmentFeeAmountTag.item(0).getTextContent():"";
        String operationalFeeAmount = operationalFeeAmountTag.getLength()>0?operationalFeeAmountTag.item(0).getTextContent():"";
        String intermediationRateAmount = intermediationRateAmountTag.getLength()>0?intermediationRateAmountTag.item(0).getTextContent():"";
        String intermediationFeeAmount = intermediationFeeAmountTag.getLength()>0?intermediationFeeAmountTag.item(0).getTextContent():"";

        String sender_name = null;
        String sender_email = null;
        String sender_areaCode = null;
        String sender_number = null;

        if(senderTag.getLength()>0){
            sender_name = senderTag.item(0).getChildNodes().getLength()>0?senderTag.item(0).getChildNodes().item(0).getTextContent():"";
            sender_email = senderTag.item(0).getChildNodes().getLength()>1?senderTag.item(0).getChildNodes().item(1).getTextContent():"";
            sender_areaCode = senderTag.item(0).getChildNodes().getLength()>2?senderTag.item(0).getChildNodes().item(2).getFirstChild().getTextContent():"";
            sender_number =senderTag.item(0).getChildNodes().getLength()>2?senderTag.item(0).getChildNodes().item(2).getLastChild().getTextContent():"";
        }

        if(order==null){
            order = MongoService.findOrderById(reference);
        }
        Order.PagSeguroInfo pagSeguroInfo =  order.new PagSeguroInfo();
        pagSeguroInfo.setCode(code);
        pagSeguroInfo.setReference(reference);
        pagSeguroInfo.setType(Utils.PagseguroTypeCompra.getTypeByCode(Integer.parseInt(type)).name());
        pagSeguroInfo.setCancellationSource(cancellationSource);
        pagSeguroInfo.setDate(date.toDate());
        pagSeguroInfo.setDiscountAmount(discountAmount);
        pagSeguroInfo.setEscrowEndDate((escrowEndDate!=null)?escrowEndDate.toDate():null);
        pagSeguroInfo.setExtraAmount(extraAmount);
        pagSeguroInfo.setGrossAmount(grossAmount);
        pagSeguroInfo.setInstallmentCount(installmentCount);
        pagSeguroInfo.setInstallmentFeeAmount(installmentFeeAmount);
        pagSeguroInfo.setIntermediationFeeAmount(intermediationFeeAmount);
        pagSeguroInfo.setIntermediationRateAmount(intermediationRateAmount);
        pagSeguroInfo.setLastEventDate((lastEventDate!=null)?lastEventDate.toDate():null);
        pagSeguroInfo.setNetAmount(netAmount);
        pagSeguroInfo.setOperationalFeeAmount(operationalFeeAmount);
        pagSeguroInfo.setPaymentMethodCode(Utils.PagseguroPagamentoCodeCompra.getCompraByCode(Integer.parseInt(paymentMethodTag_code)).name());
        pagSeguroInfo.setPaymentMethodType(Utils.PagseguroPagamentoTypeCompra.getTypeByCode(Integer.parseInt(paymentMethodTag_type)).name());
        pagSeguroInfo.setSenderAreaCode(sender_areaCode);
        pagSeguroInfo.setSenderEmail(sender_email);
        pagSeguroInfo.setSenderName(sender_name);
        pagSeguroInfo.setSenderNumber(sender_number);
        //save
        MongoService.upDateOrder(reference, pagSeguroInfo);

        order.setPagSeguroInfo(pagSeguroInfo);

        return order;
    }


    public static void updateInventoryAndDiscountCodeWithStatus(StatusOrder newStatus,Order order){

        switch (newStatus.getStatus()){
            case AGUARDANDO:
                break;
            case EMANALISE:
                break;
            case PAGO:
                if (order.isAbleToUpdateInventory(newStatus)) {
                    updateInventory(order,false);
                    updateDiscountCode(order,false);
                }
                break;
            case DISPONIVEL:
                break;
            case EMDISPUTA:
                break;
            case DEVOLVIDA:
                if (order.isAbleToUpdateInventory(newStatus)) {
                    updateInventory(order,true);
                    updateDiscountCode(order,true);
                }
                break;
            case CANCELADO:
                if (order.isAbleToUpdateInventory(newStatus)) {
                    updateInventory(order,true);
                    updateDiscountCode(order,true);
                }
                break;
        }
     }


    public static void updateDiscountCode(Order order,boolean devolvida){
        try{

            int multi = devolvida?1:-1;

            if(order.getDiscountCode()!=null){
                DiscountCode discountCode = MongoService.findDiscountCodeById(order.getDiscountCode().getCode());
                if(!discountCode.isNoTimesLimits()){
                    discountCode.setTimesLeft(discountCode.getTimesLeft()+(multi*1));
                }
                discountCode.setTimesUsed(discountCode.getTimesUsed()-(multi*1));
                MongoService.saveDiscountCode(discountCode);
            }

        }catch(Exception e){
            Logger.debug("error on update Discount Code");
        }

    }

    public static void updateInventory(Order order,boolean devolvida){
        try{

            for(Order.InventoryOrder vendido :order.getProducts()){

                Inventory inventoryDB = MongoService.findInventoryById(vendido.getSku());
                if(inventoryDB!=null){
                    try {
                        int multi = devolvida?1:-1;

                        int oldQuantity = inventoryDB.getQuantity();
                        int quantityVendida = vendido.getQuantity();
                        InventoryEntry entry = new InventoryEntry();

                        entry.setInventory(inventoryDB);
                        entry.setQuantity(multi*quantityVendida);
                        entry.setOrderId(order.getId());

                        // Save Inventory
                        inventoryDB.setQuantity(oldQuantity+multi*quantityVendida);
                        MongoService.saveInventory(inventoryDB);
                        // Save Inventory Entry
                        MongoService.saveInventoryEntry(entry);
                    }catch (Exception e) {
                    }
                }
            }
        }catch(Exception e){
            Logger.debug("error on update inventory Code");

        }

    }

    public static String generateCode(){
        return "";
    }



}
