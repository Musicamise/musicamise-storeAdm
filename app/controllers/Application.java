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

public class Application extends Controller {

    /**
     * Create
     * 
     */
    @AddCSRFToken
    public static Result login() {
        String message = flash("signin-admin");

        return ok(views.html.login.render(message));
    }
    
    @Security.Authenticated(Secured.class)
    public static Result logout() {
        session().clear();
        return redirect(routes.Application.dashboard());
    }

    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result dashboard(){
        DateTime startDate = new DateTime();
        DateTime endDate = new DateTime();
        AggregationResults<DBObject> aggResultsProducts = null;
        AggregationResults<DBObject> aggResultsSize = null;
        AggregationResults<DBObject> aggResultsGender = null;
        AggregationResults<DBObject> aggResultsEntry = null;
        AggregationResults<DBObject> aggResultsOrder = null;
        AggregationResults<DBObject> aggResultsOrderPrice = null;
        List<Order> ordersByDate  = null;
        // aggResultsProducts = MongoService.getDashboardProducts(startDate.minusMonths(1),endDate);
        // List<DBObject> products = aggResultsProducts.getMappedResults();
        // BasicDBList data = (BasicDBList)products.get(0).get("data");
        return ok(dashboardTrue.render(null));
    }

    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result content(String id){
        SiteContent contentObject = MongoService.findContentById(id);
        contentObject = (contentObject!=null?contentObject:new SiteContent());
        return ok(newContent.render(contentObject));
    }


    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result product(String id){
        Product product = MongoService.findProductById(id);
        product= (product!=null?product:new Product());
        List<Collection> collections = MongoService.getAllCollections();
        List<String> tags = MongoService.getAllTags().stream().map(Tag::getSlug).collect(Collectors.toList());
        List<DiscountCode> discounts = MongoService.findDiscountCodeByProduct(product);
        List<LocalStore> localStores = MongoService.getAllLocalStores();
        return ok(newProduct.render(product,collections,tags,localStores,discounts));
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result inventory(String id){
        Inventory inventory = MongoService.findInventoryById(id);
        inventory= (inventory!=null?inventory:new Inventory());
        List<Product> products = MongoService.getAllProducts();
        List<Collection> genders = MongoService.findCollectionByGender();
        return ok(newInventory.render(inventory,products,genders));
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result costumer(String id){
        User user = MongoService.findUserById(id);
        user= (user!=null?user:new User());
        List<String> userTags = MongoService.getAllTags().stream().map(Tag::getSlug).collect(Collectors.toList());
        return ok(newCostumer.render(user,userTags));
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result collection(String id){
        Collection collection = MongoService.findCollectionById(id);
        collection= (collection!=null?collection:new Collection());

        List<String> collectionsSlug = new ArrayList<>();
        collectionsSlug.add(collection.getSlug());
        List<Product> productsByCollections = MongoService.findProductByCollectionSlug(collectionsSlug);
        List<Product> products = MongoService.getAllProducts();

        return ok(newCollection.render(collection,productsByCollections,products));
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result tag(String id){
        Tag tag = MongoService.findTagById(id);
        tag= (tag!=null?tag:new Tag());

        List<String> tagsSlug = new ArrayList<>();
        tagsSlug.add(tag.getSlug());
        List<Product> productsByTags = MongoService.findProductByTagsSlug(tagsSlug);
        List<Product> products = MongoService.getAllProducts();

        return ok(newTag.render(tag,productsByTags,products));
    }

     @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result localStore(String id){
        LocalStore localStore = MongoService.findLocalStoreById(id);
        localStore= (localStore!=null?localStore:new LocalStore());

        List<String> localStoreSlug = new ArrayList<>();
        localStoreSlug.add(localStore.getSlug());
        List<Product> productsByLocalStore = MongoService.findProductByLocalStoreSlug(localStoreSlug);
        List<Product> products = MongoService.getAllProducts();

        return ok(newLocalStore.render(localStore,productsByLocalStore,products));
    }

    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result discountCode(String id){
        DiscountCode discountCode = MongoService.findDiscountCodeById(id);
        discountCode= (discountCode!=null?discountCode:new DiscountCode());
        List<Collection> collections =  MongoService.getAllCollections();
        List<Product> products = MongoService.getAllProducts();

        return ok(newDiscountCode.render(discountCode,collections,products));
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result giftCard(String id){
        GiftCard giftCard = MongoService.findGiftCardById(id);
        giftCard= (giftCard!=null?giftCard:new GiftCard());
        List<User> users = MongoService.getAllUsers();

        return ok(newGiftCode.render(giftCard,users));
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result order(String id){
        Order order = MongoService.findOrderById(id);
        if(order!=null){
            return ok(detailOrder.render(order));
        }else if(id!=null&&order==null){
            return redirect(routes.Application.listOrders());
        }else{
            order = new Order();
            List<User> users = MongoService.getAllUsers();
            List<Inventory> inventories = MongoService.getAllInventories();
            List<GiftCard> giftCards = MongoService.getAllGiftCards();
            List<DiscountCode> discountCodes = MongoService.getAllDiscountCodes();
            return ok(newOrder.render(order,inventories,users,discountCodes,giftCards));
        }
    }


    /**
     * List
     * */
    @Security.Authenticated(Secured.class)
    public static Result listContent() {
        List<SiteContent> contents = MongoService.getAllContents();
        return ok(listContent.render(contents));
    }
    @Security.Authenticated(Secured.class)
    public static Result listProduct() {
        List<Product> products = MongoService.getAllProducts();
        return ok(listProduct.render(products));
    }
    @Security.Authenticated(Secured.class)
    public static Result listInventory() {
        List<Inventory> inventories = MongoService.getAllInventories();

        return ok(listInventory.render(inventories));
    }
    @Security.Authenticated(Secured.class)
    public static Result listCollections() {
        List<Collection> collections = MongoService.getAllCollections();

        return ok(listCollections.render(collections));
    }
    @Security.Authenticated(Secured.class)
    public static Result listTags() {
        List<Tag> tags = MongoService.getAllTags();

        return ok(listTags.render(tags));
    }

    @Security.Authenticated(Secured.class)
    public static Result listLocalStores() {
        List<LocalStore> localStores = MongoService.getAllLocalStores();

        return ok(listLocalStores.render(localStores));
    }

    @Security.Authenticated(Secured.class)
    public static Result listGiftCard() {
        List<GiftCard> giftCards = MongoService.getAllGiftCards();
        return ok(listGiftCard.render(giftCards));
    }
    @Security.Authenticated(Secured.class)
    public static Result listDiscount() {
        List<DiscountCode> discounts = MongoService.getAllDiscountCodes();

        return ok(listDiscountCode.render(discounts));
    }
    @Security.Authenticated(Secured.class)
    public static Result listCostumers() {
        List<User> users = MongoService.getAllUsers();
        return ok(listCostumer.render(users));
    }
    @Security.Authenticated(Secured.class)
    public static Result listOrders() {
        List<Order> orders = MongoService.getAllOrders();
        return ok(listOrders.render(orders));
    }


    /**
     * save or Edit From Form TODO
     * */
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
        double total = Application.calculateFinalPrice(inventories,discountCode,null);
        DecimalFormat format = new DecimalFormat("0.00");
        String formatted = format.format(total);
        return ok(Json.toJson(formatted));
    }


    @Security.Authenticated(Secured.class)
    public static Result getDiscounteCodeApplicable(String sku){
        Product product = MongoService.findInventoryById(sku).getProduct();
        List<DiscountCode> discountCodes = MongoService.findDiscountCodeByProduct(product);
        JsonNode json = Json.toJson(discountCodes);
        return ok(json);
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
            return redirect(routes.Application.listOrders());
        }

        //Validation

        if(id!=null&&!id.equals("")&&!MongoService.hasOrderById(id)) {
            flash("listOrders","Order not in base");
            return redirect(routes.Application.listOrders());
        }

        if(productIds==null||(productIds!=null&&productIds.length<=0)){
            flash("order","Insert A product");
            return redirect(routes.Application.order(null));
        }

        if(email==null||(email!=null&&email.equals(""))){
            flash("order","Insert a user or email");
            return redirect(routes.Application.order(null));
        }
        if(user==null&&(email==null||(email!=null&&email.equals("")))){
            flash("order","Insert a user or email");
            return redirect(routes.Application.order(null));   
        }

  
      
        //validation and inventories changes on quantity
        if(productIds.length>0){
            for(int i=0;i<productIds.length;i++){
                try{
                    Inventory inventory = MongoService.findInventoryById(productIds[i]);
                    int quantity =  Integer.parseInt(quantities[i].trim());
                    if(quantity<=0||inventory.getQuantity()<quantity){
                        flash("order","Inventory without quantity or quantity <= 0");
                        return redirect(routes.Application.order(null));
                    }else{
                        inventory.setQuantity(inventory.getQuantity() -quantity );
                        inventories.add(inventory);

                        Inventory inventoryOrder = new Inventory(inventory);
                        inventoryOrder.setQuantity(quantity);
                        
                        order.getProducts().add(inventoryOrder);

                    }
                }catch(Exception e){
                    flash("order","Inventory error "+e.toString());
                    return redirect(routes.Application.order(null));
                }
            }
        }


        double total = Application.calculateFinalPrice(inventories,discountCode,giftCard);

        

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

        return redirect(routes.Application.listOrders());
    } 
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveContent(String id){

        Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        String title = (dataFiles.asFormUrlEncoded().get("title") != null && dataFiles.asFormUrlEncoded().get("title").length > 0) ? dataFiles.asFormUrlEncoded().get("title")[0] : null;
        String contentStr = (dataFiles.asFormUrlEncoded().get("content") != null && dataFiles.asFormUrlEncoded().get("content").length > 0) ? dataFiles.asFormUrlEncoded().get("content")[0] : null;
        String visible = (dataFiles.asFormUrlEncoded().get("visible") != null && dataFiles.asFormUrlEncoded().get("visible").length > 0) ? dataFiles.asFormUrlEncoded().get("visible")[0] : null;
       
        String email = (dataFiles.asFormUrlEncoded().get("email") != null && dataFiles.asFormUrlEncoded().get("email").length > 0) ? dataFiles.asFormUrlEncoded().get("email")[0] : null;
        String facebook = (dataFiles.asFormUrlEncoded().get("facebook") != null && dataFiles.asFormUrlEncoded().get("facebook").length > 0) ? dataFiles.asFormUrlEncoded().get("facebook")[0] : null;
        String twitter = (dataFiles.asFormUrlEncoded().get("twitter") != null && dataFiles.asFormUrlEncoded().get("twitter").length > 0) ? dataFiles.asFormUrlEncoded().get("twitter")[0] : null;
        String gPlus = (dataFiles.asFormUrlEncoded().get("gPlus") != null && dataFiles.asFormUrlEncoded().get("gPlus").length > 0) ? dataFiles.asFormUrlEncoded().get("gPlus")[0] : null;
        String instagram = (dataFiles.asFormUrlEncoded().get("instagram") != null && dataFiles.asFormUrlEncoded().get("instagram").length > 0) ? dataFiles.asFormUrlEncoded().get("instagram")[0] : null;

        

        boolean visibleBool = (visible!=null)?true:false;

        List<Http.MultipartFormData.FilePart> fileImages = dataFiles.getFiles();

        //Validation

        if(id!=null&&!MongoService.hasContentById(id)) {
            flash("content","Content not in base");
            return redirect(routes.Application.listContent());
        }

        //build product object
        SiteContent content = null;
        if(id!=null) {
            content = MongoService.findContentById(id);
            if(content==null){
                flash("content","Content not in base");
                return redirect(routes.Application.listContent());
            }
        }


        content.setTitle((title != null && !title.equals("")) ? title : content.getTitle());
        content.setContent((contentStr != null && !contentStr.equals("")) ? contentStr : content.getContent());
        
        content.setVisible(visibleBool);

        content.setEmail((email != null && !email.equals("")) ? email : content.getEmail());
        content.setFacebook((facebook != null && !facebook.equals("")) ? facebook : content.getFacebook());
        content.setTwitter((twitter != null && !twitter.equals("")) ? twitter : content.getTwitter());
        content.setGPlus((gPlus != null && !gPlus.equals("")) ? gPlus : content.getGPlus());
        content.setInstagram((instagram != null && !instagram.equals("")) ? instagram : content.getInstagram());
        

        String[] imageSubTitles = (dataFiles.asFormUrlEncoded().get("subtitle") != null && dataFiles.asFormUrlEncoded().get("subtitle").length > 0) ? dataFiles.asFormUrlEncoded().get("subtitle") : null;
        String[] imageUrlRedirect = (dataFiles.asFormUrlEncoded().get("urlRedirect") != null && dataFiles.asFormUrlEncoded().get("urlRedirect").length > 0) ? dataFiles.asFormUrlEncoded().get("urlRedirect") : null;
        String[] promotion = (dataFiles.asFormUrlEncoded().get("promotion") != null && dataFiles.asFormUrlEncoded().get("promotion").length > 0) ? dataFiles.asFormUrlEncoded().get("promotion") : null;

        List<Image> imagesNew = new ArrayList<>();
        for(int i = 0 ; i<fileImages.size() ;i++){
        // for(Http.MultipartFormData.FilePart file : fileImages){
            Http.MultipartFormData.FilePart file = fileImages.get(i);
            String  imageName;
            if (file != null) {
                Image image = new Image();
                imageName = file.getFilename();
                File fileSave = file.getFile();
                double bytes = fileSave.length();
                double kilobytes = (bytes / 1024);
                double megabytes = (kilobytes / 1024);

                image.setSize(megabytes+" mb");
                image.setName(imageName);
                image.setImageFile(fileSave);
                if(promotion.length > i)
                    image.setPromotion(promotion[i]!=null);
                if(imageSubTitles!=null &&imageSubTitles.length > i && imageSubTitles[i]!=null){
                    image.setSubtitle(imageSubTitles[i]);
                }
                if(imageUrlRedirect!=null &&imageUrlRedirect.length > i && imageUrlRedirect[i]!=null){
                    image.setRedirectUrl(imageUrlRedirect[i]);
                }

                image.saveFile();
                imagesNew.add(image);
            }
        }
        if(content.getImages()==null){
            content.setImages(new ArrayList<>());
        }
        content.getImages().addAll(imagesNew);
        MongoService.saveContent(content);

        return redirect(routes.Application.listContent());
    }
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveProduct(String id){

        Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        String title = (dataFiles.asFormUrlEncoded().get("title") != null && dataFiles.asFormUrlEncoded().get("title").length > 0) ? dataFiles.asFormUrlEncoded().get("title")[0] : null;
        String description = (dataFiles.asFormUrlEncoded().get("description") != null && dataFiles.asFormUrlEncoded().get("description").length > 0) ? dataFiles.asFormUrlEncoded().get("description")[0] : null;
        String price = (dataFiles.asFormUrlEncoded().get("price") != null && dataFiles.asFormUrlEncoded().get("price").length > 0) ? dataFiles.asFormUrlEncoded().get("price")[0] : null;
        String priceCompareWith = (dataFiles.asFormUrlEncoded().get("priceCompareWith") != null && dataFiles.asFormUrlEncoded().get("priceCompareWith").length > 0) ? dataFiles.asFormUrlEncoded().get("priceCompareWith")[0] : null;
        String online = (dataFiles.asFormUrlEncoded().get("online") != null && dataFiles.asFormUrlEncoded().get("online").length > 0) ? dataFiles.asFormUrlEncoded().get("online")[0] : null;
        String store = (dataFiles.asFormUrlEncoded().get("store") != null && dataFiles.asFormUrlEncoded().get("store").length > 0) ? dataFiles.asFormUrlEncoded().get("store")[0] : null;
        String newProduct = (dataFiles.asFormUrlEncoded().get("newProduct") != null && dataFiles.asFormUrlEncoded().get("newProduct").length > 0) ? dataFiles.asFormUrlEncoded().get("newProduct")[0] : null;
        String weight = (dataFiles.asFormUrlEncoded().get("weight") != null && dataFiles.asFormUrlEncoded().get("weight").length > 0) ? dataFiles.asFormUrlEncoded().get("weight")[0] : null;
        String mail = (dataFiles.asFormUrlEncoded().get("mail") != null && dataFiles.asFormUrlEncoded().get("mail").length > 0) ? dataFiles.asFormUrlEncoded().get("mail")[0] : null;
        String[] tags = (dataFiles.asFormUrlEncoded().get("tags") != null && dataFiles.asFormUrlEncoded().get("tags").length > 0) ? dataFiles.asFormUrlEncoded().get("tags") : null;
        String productType = (dataFiles.asFormUrlEncoded().get("productType") != null && dataFiles.asFormUrlEncoded().get("productType").length > 0) ? dataFiles.asFormUrlEncoded().get("productType")[0] : null;
        String[] collections = (dataFiles.asFormUrlEncoded().get("collections") != null && dataFiles.asFormUrlEncoded().get("collections").length > 0) ? dataFiles.asFormUrlEncoded().get("collections") : null;
        String color = (dataFiles.asFormUrlEncoded().get("color") != null && dataFiles.asFormUrlEncoded().get("color").length > 0) ? dataFiles.asFormUrlEncoded().get("color")[0] : null;

        String[] localStores = (dataFiles.asFormUrlEncoded().get("localStores") != null && dataFiles.asFormUrlEncoded().get("localStores").length > 0) ? dataFiles.asFormUrlEncoded().get("localStores") : null;

        // String hasDiscount = (dataFiles.asFormUrlEncoded().get("hasDiscount") != null && dataFiles.asFormUrlEncoded().get("hasDiscount").length > 0) ? dataFiles.asFormUrlEncoded().get("hasDiscount")[0] : null;
        // String discountType = (dataFiles.asFormUrlEncoded().get("discountType") != null && dataFiles.asFormUrlEncoded().get("discountType").length > 0) ? dataFiles.asFormUrlEncoded().get("discountType")[0] : null;
        // String valueOf = (dataFiles.asFormUrlEncoded().get("valueOf") != null && dataFiles.asFormUrlEncoded().get("valueOf").length > 0) ? dataFiles.asFormUrlEncoded().get("valueOf")[0] : null;
       

        // boolean hasDiscountBool = (hasDiscount!=null)?true:false;
        // double valueOfDiscountDouble = Double.parseDouble(valueOf);
        // Utils.DiscountType typeDiscount = Utils.DiscountType.valueOf(discountType);



        boolean onlineBool = (online!=null)?true:false;
        boolean storeBool = (store!=null)?true:false;
        boolean newProductBool = (newProduct!=null)?true:false;
        boolean mailBool = (mail!=null)?true:false;

        double priceDouble = Double.parseDouble(price);
        double priceCompareWithDouble = Double.parseDouble(priceCompareWith);
        double weightDouble = Double.parseDouble(weight);

        Set<String> tagsList =  (tags!=null)?new HashSet<>(Arrays.asList(tags)):new HashSet<>();
        Set<String> collectionsList =  (collections!=null)?new HashSet<>(Arrays.asList(collections)):new HashSet<>();

        Set<String> localStoresList =  (localStores!=null)?new HashSet<>(Arrays.asList(localStores)):new HashSet<>();


        List<Http.MultipartFormData.FilePart> fileImages = dataFiles.getFiles();



        //Validation

        if(id!=null&&!MongoService.hasProductById(id)) {
            flash("product","Procut not in base");
            return redirect(routes.Application.product(null));
        }

        if(title==null||title.equals("")){
            flash("product","Insert Title at least");
            return redirect(routes.Application.product(null));
        }

        //build product object
        Product product = null;
        if(id!=null) {
            product = MongoService.findProductById(id);
        }

        product = (product!=null)?product:new Product();

        product.setTitle((title != null && !title.equals("")) ? title : product.getTitle());
        product.setDescription((description != null && !description.equals("")) ? title : product.getDescription());
        product.setNewProduct(newProductBool);
        product.setOnLineVisible(onlineBool);
        product.setPrice(priceDouble);
        product.setPriceCompareWith(priceCompareWithDouble);
        product.setSendMail(mailBool);
        product.setStoreVisible(storeBool);
        product.setType((productType != null && !productType.equals("")) ? productType : product.getType());
        product.setUserTags(tagsList);
        product.setWeight(weightDouble);
        product.setCollectionsSlugs(collectionsList);
        product.setLocalStoresSlugs(localStoresList);
        product.setColor(color);


        // product.setHasDiscount(hasDiscountBool);
        // product.setDiscount(valueOfDiscountDouble);
        // product.setDiscountType(typeDiscount);


        List<Image> imagesNew = new ArrayList<>();
        for(Http.MultipartFormData.FilePart file : fileImages){
            String  imageName;
            if (file != null) {
                Image image = new Image();
                imageName = file.getFilename();
                File fileSave = file.getFile();
                double bytes = fileSave.length();
                double kilobytes = (bytes / 1024);
                double megabytes = (kilobytes / 1024);

                image.setSize(megabytes+" mb");
                image.setName(imageName);
                image.setImageFile(fileSave);
                image.saveFile();
                imagesNew.add(image);
            }
        }

        product.getImages().addAll(imagesNew);
        MongoService.saveProduct(product);

        return redirect(routes.Application.listProduct());
    }
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveInventory(String id){

        //Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String productId = (dataFiles.get("product") != null && dataFiles.get("product").length > 0) ? dataFiles.get("product")[0] : null;
        String outOfStock = (dataFiles.get("outOfStock") != null && dataFiles.get("outOfStock").length > 0) ? dataFiles.get("outOfStock")[0] : null;
        String productSize = (dataFiles.get("productSize") != null && dataFiles.get("productSize").length > 0) ? dataFiles.get("productSize")[0] : null;
        String quantity = (dataFiles.get("quantity") != null && dataFiles.get("quantity").length > 0) ? dataFiles.get("quantity")[0] : null;
        String sellInOutOfStock = (dataFiles.get("sellInOutOfStock") != null && dataFiles.get("sellInOutOfStock").length > 0) ? dataFiles.get("sellInOutOfStock")[0] : null;
        String gender = (dataFiles.get("gender") != null && dataFiles.get("gender").length > 0) ? dataFiles.get("gender")[0] : null;

        boolean outOfStockBool = (outOfStock!=null)?true:false;
        boolean sellInOutOfStockBool = (sellInOutOfStock!=null)?true:false;


        int quantityInt = Integer.parseInt(quantity.trim().replace(".",""));

        //Validation

        if(id!=null&&!MongoService.hasInventoryById(id)) {
            flash("inventory","Inventory not in base");
            return redirect(routes.Application.inventory(null));
        }

        if(productId==null||productId.equals("")){
            flash("inventory","Insert A product");
            return redirect(routes.Application.inventory(id));
        }

        if(productId!=null&&!MongoService.hasProductById(productId)){
            flash("inventory","Product does not Exist");
            return redirect(routes.Application.inventory(id));
        }
        if(gender!=null&&!gender.equals("")&&!MongoService.hasCollectionByGender(gender)){
            flash("inventory","Gender dont exists or Gender empty ");
            return redirect(routes.Application.inventory(id));
        }
        if(productSize!=null&&!productSize.equals("")&&!Utils.ProductSizeType.getList().contains(productSize)){
            flash("inventory","Product Size dont exists or Product Size empty ");
            return redirect(routes.Application.inventory(id));
        }

        if(id==null&&MongoService.hasInventoryByProductIdSizeAndGender(productId,productSize,gender)){
            flash("inventory","Inventory Already exists please update");
            return redirect(routes.Application.inventory(id));
        }


        //build inventory object
        Inventory inventory = null;
        if(id!=null) {
            inventory = MongoService.findInventoryById(id);
        }
        Product product = MongoService.findProductById(productId);

        inventory = (inventory!=null)?inventory:new Inventory();
        inventory.setOrderOutOfStock(outOfStockBool);
        inventory.setProduct(product);
        int oldQuantity = inventory.getQuantity();
        inventory.setQuantity(quantityInt);
        inventory.setSize(productSize);
        inventory.setSellInOutOfStock(sellInOutOfStockBool);
        inventory.setGenderSlug(gender);

        //save Inventory
        MongoService.saveInventory(inventory);
        // Save Inventory Entry
        
        boolean updatedQuantity = ((oldQuantity - quantityInt)==0)?false:true;

        if(updatedQuantity){
            InventoryEntry entry = new InventoryEntry();
            entry.setInventory(inventory);
            if(id!=null){
                entry.setQuantity(quantityInt - oldQuantity);
            }else{
                entry.setQuantity(quantityInt);
            }
            MongoService.saveInventoryEntry(entry);
        }

        //product update
        product.getInventories().add(inventory);
        MongoService.saveProduct(product);

        return redirect(routes.Application.inventory(id));
    }
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveCollection(String id){

        Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        String title = (dataFiles.asFormUrlEncoded().get("title") != null && dataFiles.asFormUrlEncoded().get("title").length > 0) ? dataFiles.asFormUrlEncoded().get("title")[0] : null;
        String description = (dataFiles.asFormUrlEncoded().get("description") != null && dataFiles.asFormUrlEncoded().get("description").length > 0) ? dataFiles.asFormUrlEncoded().get("description")[0] : null;
        String visibleOnline = (dataFiles.asFormUrlEncoded().get("visibleOnline") != null && dataFiles.asFormUrlEncoded().get("visibleOnline").length > 0) ? dataFiles.asFormUrlEncoded().get("visibleOnline")[0] : null;
        String hasOnLocalStore = (dataFiles.asFormUrlEncoded().get("hasOnLocalStore") != null && dataFiles.asFormUrlEncoded().get("hasOnLocalStore").length > 0) ? dataFiles.asFormUrlEncoded().get("hasOnLocalStore")[0] : null;
        String[] products = (dataFiles.asFormUrlEncoded().get("products") != null && dataFiles.asFormUrlEncoded().get("products").length > 0) ? dataFiles.asFormUrlEncoded().get("products") : null;

        String mainMenu = (dataFiles.asFormUrlEncoded().get("mainMenu") != null && dataFiles.asFormUrlEncoded().get("mainMenu").length > 0) ? dataFiles.asFormUrlEncoded().get("mainMenu")[0] : null;
        String gender = (dataFiles.asFormUrlEncoded().get("gender") != null && dataFiles.asFormUrlEncoded().get("gender").length > 0) ? dataFiles.asFormUrlEncoded().get("gender")[0] : null;

        boolean onlineBool = (visibleOnline!=null)?true:false;
        boolean storeBool = (hasOnLocalStore!=null)?true:false;
        boolean mainMenuBool = (mainMenu!=null)?true:false;
        boolean genderBool = (gender!=null)?true:false;

        List<String> productsList =  (products!=null)?Arrays.asList(products):new ArrayList<>();

        List<Http.MultipartFormData.FilePart> fileImages = dataFiles.getFiles();



        //Validation

        if(id!=null&&!MongoService.hasCollectionById(id)) {
            flash("collection","Procut not in base");
            return redirect(routes.Application.collection(null));
        }

        if(title==null||title.equals("")){
            flash("collection","Insert Title at least");
            return redirect(routes.Application.collection(null));
        }

        //build product object
        Collection collection = null;
        if(id!=null) {
            collection = MongoService.findCollectionById(id);
        }

        collection = (collection!=null)?collection:new Collection();

        collection.setTitle((title != null && !title.equals("")) ? title : collection.getTitle());
        collection.setDescription(description);
        collection.setOnLineVisible(onlineBool);
        collection.setOnLocalStore(storeBool);
        collection.setMainMenu(mainMenuBool);
        collection.setGender(genderBool);



        List<String> collectionsSlug = new ArrayList<>();
        collectionsSlug.add(collection.getSlug());
        final String slugFinal = collection.getSlug();

        List<Product> productsToAddOrRemoveCollection = MongoService.findProductByCollectionSlugOrListId(collectionsSlug, productsList);
        productsToAddOrRemoveCollection.stream().forEach(p->{
            if(productsList.contains(p.getId())){
                p.getCollectionsSlugs().add(slugFinal);
            }else{
                p.getCollectionsSlugs().remove(slugFinal);}
        });
        MongoService.saveProducts(productsToAddOrRemoveCollection);
        List<Image> imagesNew = new ArrayList<>();
        for(Http.MultipartFormData.FilePart file : fileImages){
            String  imageName;
            if (file != null) {
                Image image = new Image();
                imageName = file.getFilename();
                File fileSave = file.getFile();
                double bytes = fileSave.length();
                double kilobytes = (bytes / 1024);
                double megabytes = (kilobytes / 1024);

                image.setSize(megabytes+" mb");
                image.setName(imageName);
                image.setImageFile(fileSave);
                image.saveFile();
                imagesNew.add(image);
            }
        }

        if(imagesNew.size() > 0&&collection.getImage()!=null) {
            collection.getImage().deleteFile();
        }
        collection.setImage((imagesNew.size() > 0) ? imagesNew.get(0) : collection.getImage());

        MongoService.saveCollection(collection);

        return redirect(routes.Application.listCollections());
    }
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveTag(String id){

        Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        String title = (dataFiles.asFormUrlEncoded().get("title") != null && dataFiles.asFormUrlEncoded().get("title").length > 0) ? dataFiles.asFormUrlEncoded().get("title")[0] : null;
        String description = (dataFiles.asFormUrlEncoded().get("description") != null && dataFiles.asFormUrlEncoded().get("description").length > 0) ? dataFiles.asFormUrlEncoded().get("description")[0] : null;
        String visibleOnline = (dataFiles.asFormUrlEncoded().get("visibleOnline") != null && dataFiles.asFormUrlEncoded().get("visibleOnline").length > 0) ? dataFiles.asFormUrlEncoded().get("visibleOnline")[0] : null;
        String hasOnLocalStore = (dataFiles.asFormUrlEncoded().get("hasOnLocalStore") != null && dataFiles.asFormUrlEncoded().get("hasOnLocalStore").length > 0) ? dataFiles.asFormUrlEncoded().get("hasOnLocalStore")[0] : null;
        String[] products = (dataFiles.asFormUrlEncoded().get("products") != null && dataFiles.asFormUrlEncoded().get("products").length > 0) ? dataFiles.asFormUrlEncoded().get("products") : null;

        boolean onlineBool = (visibleOnline!=null)?true:false;
        boolean storeBool = (hasOnLocalStore!=null)?true:false;

        List<String> productsList =  (products!=null)?Arrays.asList(products):new ArrayList<>();

        List<Http.MultipartFormData.FilePart> fileImages = dataFiles.getFiles();



        //Validation

        if(id!=null&&!MongoService.hasTagById(id)) {
            flash("tag","Procut not in base");
            return redirect(routes.Application.tag(null));
        }

        if(title==null||title.equals("")){
            flash("tag","Insert Title at least");
            return redirect(routes.Application.tag(null));
        }

        //build product object
        Tag tag = null;
        if(id!=null) {
            tag = MongoService.findTagById(id);
        }

        tag = (tag!=null)?tag:new Tag();

        tag.setTitle((title != null && !title.equals("")) ? title : tag.getTitle());
        tag.setDescription(description);
        tag.setOnLineVisible(onlineBool);
        tag.setOnLocalStore(storeBool);


        List<String> tagsSlug = new ArrayList<>();
        tagsSlug.add(tag.getSlug());
        final String slugFinal = tag.getSlug();

        List<Product> productsToAddOrRemoveTag = MongoService.findProductByTagSlugOrListId(tagsSlug, productsList);
        productsToAddOrRemoveTag.stream().forEach(p->{
            if(productsList.contains(p.getId())){
                p.getUserTags().add(slugFinal);
            }else{
                p.getUserTags().remove(slugFinal);}
        });
        MongoService.saveProducts(productsToAddOrRemoveTag);
        List<Image> imagesNew = new ArrayList<>();
        for(Http.MultipartFormData.FilePart file : fileImages){
            String  imageName;
            if (file != null) {
                Image image = new Image();
                imageName = file.getFilename();
                File fileSave = file.getFile();
                double bytes = fileSave.length();
                double kilobytes = (bytes / 1024);
                double megabytes = (kilobytes / 1024);

                image.setSize(megabytes+" mb");
                image.setName(imageName);
                image.setImageFile(fileSave);
                image.saveFile();
                imagesNew.add(image);
            }
        }

        if(imagesNew.size() > 0&&tag.getImage()!=null) {
            tag.getImage().deleteFile();
        }
        tag.setImage((imagesNew.size() > 0) ? imagesNew.get(0) : tag.getImage());

        MongoService.saveTag(tag);

        return redirect(routes.Application.listTags());
    }
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveLocalStore(String id){

        Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        String title = (dataFiles.asFormUrlEncoded().get("title") != null && dataFiles.asFormUrlEncoded().get("title").length > 0) ? dataFiles.asFormUrlEncoded().get("title")[0] : null;
        String description = (dataFiles.asFormUrlEncoded().get("description") != null && dataFiles.asFormUrlEncoded().get("description").length > 0) ? dataFiles.asFormUrlEncoded().get("description")[0] : null;
        String visibleOnline = (dataFiles.asFormUrlEncoded().get("visibleOnline") != null && dataFiles.asFormUrlEncoded().get("visibleOnline").length > 0) ? dataFiles.asFormUrlEncoded().get("visibleOnline")[0] : null;
        String[] products = (dataFiles.asFormUrlEncoded().get("products") != null && dataFiles.asFormUrlEncoded().get("products").length > 0) ? dataFiles.asFormUrlEncoded().get("products") : null;

        boolean onlineBool = (visibleOnline!=null)?true:false;

        List<String> productsList =  (products!=null)?Arrays.asList(products):new ArrayList<>();

        List<Http.MultipartFormData.FilePart> fileImages = dataFiles.getFiles();



        //Validation

        if(id!=null&&!MongoService.hasLocalStoreById(id)) {
            flash("localStore","Store is not in base");
            return redirect(routes.Application.localStore(null));
        }

        if(title==null||title.equals("")){
            flash("localStore","Insert Title at least");
            return redirect(routes.Application.localStore(null));
        }

        //build product object
        LocalStore localStore = null;
        if(id!=null) {
            localStore = MongoService.findLocalStoreById(id);
        }

        localStore = (localStore!=null)?localStore:new LocalStore();

        localStore.setTitle((title != null && !title.equals("")) ? title : localStore.getTitle());
        localStore.setDescription(description);
        localStore.setOnLineVisible(onlineBool);


        List<String> localStoreSlug = new ArrayList<>();
        localStoreSlug.add(localStore.getSlug());
        final String slugFinal = localStore.getSlug();

        List<Product> productsToAddOrRemoveLocalStore = MongoService.findProductByLocalStoreSlugOrListId(localStoreSlug, productsList);
        productsToAddOrRemoveLocalStore.stream().forEach(p->{
            if(productsList.contains(p.getId())){
                p.getLocalStoresSlugs().add(slugFinal);
            }else{
                p.getLocalStoresSlugs().remove(slugFinal);}
        });
        MongoService.saveProducts(productsToAddOrRemoveLocalStore);

        List<Image> imagesNew = new ArrayList<>();
        for(Http.MultipartFormData.FilePart file : fileImages){
            String  imageName;
            if (file != null) {
                Image image = new Image();
                imageName = file.getFilename();
                File fileSave = file.getFile();
                double bytes = fileSave.length();
                double kilobytes = (bytes / 1024);
                double megabytes = (kilobytes / 1024);

                image.setSize(megabytes+" mb");
                image.setName(imageName);
                image.setImageFile(fileSave);
                image.saveFile();
                imagesNew.add(image);
            }
        }

        if(imagesNew.size() > 0&&localStore.getImage()!=null) {
            localStore.getImage().deleteFile();
        }
        localStore.setImage((imagesNew.size() > 0) ? imagesNew.get(0) : localStore.getImage());

        MongoService.saveLocalStore(localStore);

        return redirect(routes.Application.listLocalStores());
    }
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveGiftCard(String id){

        //Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String code = (dataFiles.get("code") != null && dataFiles.get("code").length > 0) ? dataFiles.get("code")[0] : null;
        String price = (dataFiles.get("price") != null && dataFiles.get("price").length > 0) ? dataFiles.get("price")[0] : null;
        String userFrom = (dataFiles.get("userFrom") != null && dataFiles.get("userFrom").length > 0) ? dataFiles.get("userFrom")[0] : null;
        String userTo = (dataFiles.get("userTo") != null && dataFiles.get("userTo").length > 0) ? dataFiles.get("userTo")[0] : null;
        String active = (dataFiles.get("active") != null && dataFiles.get("active").length > 0) ? dataFiles.get("active")[0] : null;
        String used = (dataFiles.get("used") != null && dataFiles.get("used").length > 0) ? dataFiles.get("used")[0] : null;

        boolean usedBool = (used!=null)?true:false;
        boolean activeBool = (active!=null)?true:false;

        double priceDouble = Double.parseDouble(price);




        //Validation

        if(id!=null&&!MongoService.hasGiftCardById(id)) {
            flash("giftCard","Gift Card not in base");

            return redirect(routes.Application.giftCard(null));
        }

        if(code==null||code.equals("")){
            flash("giftCard","Insert a Valid Code");
            return redirect(routes.Application.giftCard(null));
        }
        if(id==null&&MongoService.hasGiftCardById(code)){
            flash("giftCard","Insert a Valid Code");
            return redirect(routes.Application.giftCard(null));
        }


        //build inventory object
        GiftCard giftCard = null;
        if(id!=null) {
            giftCard = MongoService.findGiftCardById(id);
        }

        giftCard = (giftCard!=null)?giftCard:new GiftCard();
        giftCard.setActive(activeBool);
        giftCard.setCode((giftCard.getCode() == null) ? code : giftCard.getCode());
        giftCard.setPrice(priceDouble);
        giftCard.setUsed((giftCard.isUsed())?giftCard.isUsed():usedBool);
        giftCard.setUserIdFrom(userFrom);
        giftCard.setUserIdTo(userTo);

        //save Inventory
        MongoService.saveGiftCard(giftCard);
        return redirect(routes.Application.listGiftCard());
    }
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveDiscountCode(String id){

        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String code = (dataFiles.get("code") != null && dataFiles.get("code").length > 0) ? dataFiles.get("code")[0] : null;
        String timesLeft = (dataFiles.get("timesLeft") != null && dataFiles.get("timesLeft").length > 0) ? dataFiles.get("timesLeft")[0] : null;
        String noLimits = (dataFiles.get("noLimits") != null && dataFiles.get("noLimits").length > 0) ? dataFiles.get("noLimits")[0] : null;
        String discountType = (dataFiles.get("discountType") != null && dataFiles.get("discountType").length > 0) ? dataFiles.get("discountType")[0] : null;
        String valueOf = (dataFiles.get("valueOF") != null && dataFiles.get("valueOF").length > 0) ? dataFiles.get("valueOF")[0] : null;
        String validationType = (dataFiles.get("validationType") != null && dataFiles.get("validationType").length > 0) ? dataFiles.get("validationType")[0] : null;
        String applyCondition = (dataFiles.get("applyCondition") != null && dataFiles.get("applyCondition").length > 0) ? dataFiles.get("applyCondition")[0] : null;
        String noDateLimite = (dataFiles.get("noDateLimite") != null && dataFiles.get("noDateLimite").length > 0) ? dataFiles.get("noDateLimite")[0] : null;
        String dateRange = (dataFiles.get("dateRange") != null && dataFiles.get("dateRange").length > 0) ? dataFiles.get("dateRange")[0] : null;
        String overValueInput = (dataFiles.get("overValue") != null && dataFiles.get("overValue").length > 0) ? dataFiles.get("overValue")[0] : null;

        String active = (dataFiles.get("active") != null && dataFiles.get("active").length > 0) ? dataFiles.get("active")[0] : null;

        String[] collections = (dataFiles.get("collections") != null && dataFiles.get("collections").length > 0) ? dataFiles.get("collections") : null;
        String[] products = (dataFiles.get("products") != null && dataFiles.get("products").length > 0) ? dataFiles.get("products") : null;
        Set<String> collectionList =  (collections!=null)?new HashSet<>(Arrays.asList(collections)):new HashSet<>();
        Set<String> productsnList =  (products!=null)?new HashSet<>(Arrays.asList(products)):new HashSet<>();

        boolean noLimitsBool = (noLimits!=null)?true:false;
        boolean noDateLimiteBool = (noDateLimite!=null)?true:false;
        boolean activeBool = (active!=null)?true:false;


        Utils.DiscountValidation validation = Utils.DiscountValidation.valueOf(validationType);
        Utils.DiscountPaymentAdjust paymentAdjust = Utils.DiscountPaymentAdjust.valueOf(applyCondition);
        Utils.DiscountType typeDiscount = Utils.DiscountType.valueOf(discountType);

        String onLocalStore = (dataFiles.get("onLocalStore") != null && dataFiles.get("onLocalStore").length > 0) ? dataFiles.get("onLocalStore")[0] : null;
        String onLineVisible = (dataFiles.get("onLineVisible") != null && dataFiles.get("onLineVisible").length > 0) ? dataFiles.get("onLineVisible")[0] : null;

        boolean onLocalStoreBool = (onLocalStore!=null)?true:false;
        boolean onLineVisibleBool = (onLineVisible!=null)?true:false;




        double value = 0;
        try {
            value = Double.parseDouble((valueOf != null && !valueOf.equals("")) ? valueOf : "0");
        }catch (Exception e){

        }
        double overValueInputDouble = 0;
        try {

            overValueInputDouble = Double.parseDouble((overValueInput!=null&&!overValueInput.equals(""))?overValueInput:"0");
        }catch (Exception e){

        }
        int timesLeftInt = 0;
        try {

            timesLeftInt = Integer.parseInt((timesLeft!=null&&!timesLeft.equals(""))?timesLeft:"0");
        }catch (Exception e){
            timesLeftInt = 0;
        }

        Date dateStart = null;
        Date dateEnd = null;

        if(dateRange!=null&&!dateRange.equals("")) {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH);
            try {
                dateStart = format.parse(dateRange.split(" - ")[0]);
                dateEnd = format.parse(dateRange.split(" - ")[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        //Validation

        if(id!=null&&!MongoService.hasDiscountCodeByCode(code)){
            flash("discountCode","Code not found");
        }

        if(id==null&&code!=null&&MongoService.hasDiscountCodeByCode(code)) {
            flash("discountCode","Code already in use");
            return redirect(routes.Application.discountCode(null));
        }

        if(validation.equals(Utils.DiscountValidation.collections)&&collectionList.size()==0){
            flash("discountCode","Need Some Collections for this validation");
            return redirect(routes.Application.discountCode(null));
        }


        //build inventory object
        DiscountCode discount = null;
        if(id!=null) {
            discount = MongoService.findDiscountCodeById(id);
        }

        discount = (discount!=null)?discount:new DiscountCode();
        discount.setCode((discount.getCode() == null) ? code : discount.getCode());

        discount.setTypeForPay(typeDiscount);
        discount.setValueOf(value);
        discount.setActive(activeBool);

        discount.setNoDateLimits(noDateLimiteBool);
        discount.setStartDate(dateStart);
        discount.setEndDate(dateEnd);

        discount.setNoTimesLimits(noLimitsBool);
        discount.setTimesLeft(timesLeftInt);

        discount.setOrdersValidation(validation);
        discount.setWhereApply(paymentAdjust);

        discount.setOnLocalStore(onLocalStoreBool);
        discount.setOnLineVisible(onLineVisibleBool);


        switch (validation) {

            case collections: {
                discount.setCollectionsSlug(collectionList);
                discount.setOverValueOf(0);
                discount.setProductSlugs(new HashSet<>());
                break;
            }
            case overValue: {
                discount.setCollectionsSlug(new HashSet<>());
                discount.setOverValueOf(overValueInputDouble);
                discount.setProductSlugs(new HashSet<>());
                break;

            }
            case specificProduct:{
                discount.setCollectionsSlug(new HashSet<>());
                discount.setOverValueOf(0);
                discount.setProductSlugs(productsnList);
                break;
            }
            default:{
                discount.setCollectionsSlug(new HashSet<>());
                discount.setOverValueOf(0);
                discount.setProductSlugs(new HashSet<>());
                break;

            }

        }

        //save Discount Code
        MongoService.saveDiscountCode(discount);





        return redirect(routes.Application.listDiscount());
    }
    @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveCostumer(String id){

        //Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();
        String fullname = (dataFiles.get("fullname") != null && dataFiles.get("fullname").length > 0) ? dataFiles.get("fullname")[0] : null;
        String firstname = (dataFiles.get("firstname") != null && dataFiles.get("firstname").length > 0) ? dataFiles.get("firstname")[0] : null;
        String lastname = (dataFiles.get("lastname") != null && dataFiles.get("lastname").length > 0) ? dataFiles.get("lastname")[0] : null;
        String email = (dataFiles.get("email") != null && dataFiles.get("email").length > 0) ? dataFiles.get("email")[0] : null;
        String mktAccept = (dataFiles.get("mktAccept") != null && dataFiles.get("mktAccept").length > 0) ? dataFiles.get("mktAccept")[0] : null;
        String notes = (dataFiles.get("notes") != null && dataFiles.get("notes").length > 0) ? dataFiles.get("notes")[0] : null;
        String[] tags = (dataFiles.get("tags") != null && dataFiles.get("tags").length > 0) ? dataFiles.get("tags") : null;
        List<String> tagsList =  (tags!=null)?Arrays.asList(tags):new ArrayList<>();
        boolean mktAcceptBool = (mktAccept!=null)?true:false;

        //Validation

        if(id!=null&&!MongoService.hasUserById(id)) {
            flash("costumer","User not in base");
            return redirect(routes.Application.costumer(id!=null?id:null));
        }

        if((fullname==null||fullname.equals(""))&&(firstname==null||firstname.equals(""))){
            flash("costumer","Insert First name or Full name");
            return redirect(routes.Application.costumer(id!=null?id:null));
        }
        if(email==null||email.equals("")||!isValidEmailAddress(email)){
            flash("costumer","Isert Email correctly");
            return redirect(routes.Application.costumer(id!=null?id:null));
        }
        if (id==null&&MongoService.hasUserByEmail(email)){
            flash("costumer","Email already in use");
            return redirect(routes.Application.costumer(id!=null?id:null));
        }
        // if (id!=null&&!(id.equals(email))&&MongoService.hasUserByEmail(email)){
        //     flash("costumer","Changing the Email with other already in use");
        //     return redirect(routes.Application.costumer(id));
        // }

        //build object User
        User  user = null;

        if(id==null){
            user = new User(firstname,email,generatePassword());
        }else{
            user = MongoService.findUserById(id);
        }

        user.setFullName((fullname != null && !fullname.equals("")) ? fullname : user.getFullName());
        user.setFirstName((firstname != null && !firstname.equals("")) ? firstname : user.getFirstName());
        user.setLastName((lastname != null && !lastname.equals("")) ? lastname : user.getLastName());
        // user.setEmail((email != null && !email.equals("")) ? email : user.getEmail());
        user.setMarketingEmail(mktAcceptBool);
        user.setNotes((notes != null && !notes.equals("")) ? notes : user.getNotes());
        user.setTags(tagsList);

        int count = dataFiles.get("address") != null ? dataFiles.get("address").length : 0;
        List<Address> addressesList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Address addressObj = new Address();
            String name = (dataFiles.get("name") != null && dataFiles.get("name").length > i) ? dataFiles.get("name")[i] : null;
            String address = (dataFiles.get("address") != null && dataFiles.get("address").length > i) ? dataFiles.get("address")[i] : null;
            String number = (dataFiles.get("number") != null && dataFiles.get("number").length > i) ? dataFiles.get("number")[i] : null;
            String city = (dataFiles.get("city") != null && dataFiles.get("city").length > i) ? dataFiles.get("city")[i] : null;
            String state = (dataFiles.get("state") != null && dataFiles.get("state").length > i) ? dataFiles.get("state")[i] : null;
            String country = (dataFiles.get("country") != null && dataFiles.get("country").length > i) ? dataFiles.get("country")[i] : null;
            String zipcode = (dataFiles.get("zipcode") != null && dataFiles.get("zipcode").length > i) ? dataFiles.get("zipcode")[i] : null;
            addressObj.setName(name);
            addressObj.setAddress(address);
            addressObj.setCity(city);
            addressObj.setState(state);
            addressObj.setNumber(number);
            addressObj.setCountry(country);
            addressObj.setCep(zipcode);
            addressesList.add(addressObj);
        }

        user.setAddress(addressesList);

        //save user or update
        MongoService.saveUser(user);

        return redirect(routes.Application.listCostumers());
    }


    /**
     * save From List TODO
     * */

    @Security.Authenticated(Secured.class)
    public static Result deleteProduct(String id){
        Product product = MongoService.findProductById(id);
        if(product!=null){
            MongoService.deleteProduct(product);
            flash("listProduct","Removed with success");
        }
        return redirect(routes.Application.listProduct());
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteInventory(String id){
        Inventory inventory = MongoService.findInventoryById(id);
        if(inventory!=null){
            MongoService.deleteInventory(inventory);
            flash("listInventory","Removed with success");
        }
        return redirect(routes.Application.listInventory());
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteCollection(String id){

        Collection collection = MongoService.findCollectionById(id);
        if(collection!=null){
            MongoService.deleteCollection(collection);
            flash("listCollections","Removed with success");
        }
        return redirect(routes.Application.listCollections());
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteTag(String id){

        Tag tag = MongoService.findTagById(id);
        if(tag!=null){
            MongoService.deleteTag(tag);
            flash("listTags","Removed with success");
        }
        return redirect(routes.Application.listTags());
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteLocalStore(String id){

        LocalStore localStore = MongoService.findLocalStoreById(id);
        if(localStore!=null){
            MongoService.deleteLocalStore(localStore);
            flash("listLocalStores","Removed with success");
        }
        return redirect(routes.Application.listLocalStores());
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteGiftCard(String id){
        GiftCard giftCard = MongoService.findGiftCardById(id);
        if(giftCard!=null){
            MongoService.deleteGiftCard(giftCard);
            flash("listGiftCard","Removed with success");
        }
        return redirect(routes.Application.listGiftCard());
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteDiscountCode(String id){
        DiscountCode discountCode = MongoService.findDiscountCodeById(id);
        if(discountCode!=null){
            MongoService.deleteDiscountCode(discountCode);
            flash("listDiscountCode","Removed with success");
        }
        return redirect(routes.Application.listDiscount());
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteCostumer(String id){
        User user = MongoService.findUserByEmail(id);
        if(user!=null){
            MongoService.deleteUser(user);
            flash("listCostumers","Removed with success");
        }
        return redirect(routes.Application.listCostumers());

    }

    @Security.Authenticated(Secured.class)
    public static Result deleteProductImage(String productId,String imageName){
        if(productId!=null && imageName!=null){
            Product product = MongoService.findProductById(productId);
            if(product!=null){
                try {
                    Image image = product.getImages().stream().filter(i -> i.getName().equals(imageName)).findFirst().get();
                    product.getImages().remove(image);
                    MongoService.saveProduct(product);
                    image.deleteFile();
                    return ok();
                }catch (Exception e) {
                    return internalServerError();
                }
            }
        }
        return internalServerError();
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteSiteContentImage(String contentId,String imageName){
        if(contentId!=null && imageName!=null){
            SiteContent content = MongoService.findContentById(contentId);
            if(content!=null){
                try {
                    Image image = content.getImages().stream().filter(i -> i.getName().equals(imageName)).findFirst().get();
                    content.getImages().remove(image);
                    MongoService.saveContent(content);
                    image.deleteFile();
                    return ok();
                }catch (Exception e) {
                    return internalServerError();
                }
            }
        }
        return internalServerError();
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteCollectionImage(){
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String collectionId = (dataFiles.get("collectionId") != null && dataFiles.get("collectionId").length > 0) ? dataFiles.get("collectionId")[0] : null;
        String imageName = (dataFiles.get("imageName") != null && dataFiles.get("imageName").length > 0) ? dataFiles.get("imageName")[0] : null;


        if(collectionId!=null && imageName!=null){
            Collection collection = MongoService.findCollectionById(collectionId);
            if(collection!=null){
                try {
                    Image image = collection.getImage();
                    image.deleteFile();
                    collection.setImage(null);
                    MongoService.saveCollection(collection);

                    return ok();
                }catch (Exception e) {
                    return internalServerError();
                }
            }
        }
        return internalServerError();
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteTagImage(){
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String tagId = (dataFiles.get("tagId") != null && dataFiles.get("tagId").length > 0) ? dataFiles.get("tagId")[0] : null;
        String imageName = (dataFiles.get("imageName") != null && dataFiles.get("imageName").length > 0) ? dataFiles.get("imageName")[0] : null;


        if(tagId!=null && imageName!=null){
            Tag tag = MongoService.findTagById(tagId);
            if(tag!=null){
                try {
                    Image image = tag.getImage();
                    image.deleteFile();
                    tag.setImage(null);
                    MongoService.saveTag(tag);

                    return ok();
                }catch (Exception e) {
                    return internalServerError();
                }
            }
        }
        return internalServerError();
    }
    @Security.Authenticated(Secured.class)
    public static Result deleteLocalStoreImage(){
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String localStoreId = (dataFiles.get("localStoreId") != null && dataFiles.get("localStoreId").length > 0) ? dataFiles.get("localStoreId")[0] : null;
        String imageName = (dataFiles.get("imageName") != null && dataFiles.get("imageName").length > 0) ? dataFiles.get("imageName")[0] : null;


        if(localStoreId!=null && imageName!=null){
            LocalStore localStore = MongoService.findLocalStoreById(localStoreId);
            if(localStore!=null){
                try {
                    Image image = localStore.getImage();
                    image.deleteFile();
                    localStore.setImage(null);
                    MongoService.saveLocalStore(localStore);

                    return ok();
                }catch (Exception e) {
                    return internalServerError();
                }
            }
        }
        return internalServerError();
    }

    @Security.Authenticated(Secured.class)
    public static Result updateInventoryQuantity(String id,String quantity){
        if(id!=null && quantity!=null){
            Inventory inventory = MongoService.findInventoryById(id);
            if(inventory!=null){
                try {
                    int oldQuantity = inventory.getQuantity();
                    int quantityInt = Integer.parseInt(quantity.trim());

                    boolean updatedQuantity = ((oldQuantity - quantityInt)==0)?false:true;
        
                    if(updatedQuantity){
                        InventoryEntry entry = new InventoryEntry();
                        entry.setInventory(inventory);
                        entry.setQuantity(quantityInt - oldQuantity);

                        // Save Inventory
                        inventory.setQuantity(quantityInt);
                        MongoService.saveInventory(inventory);
                        // Save Inventory Entry
                        MongoService.saveInventoryEntry(entry);
                    }


                    return ok();
                }catch (Exception e) {
                    return internalServerError();
                }
            }
        }
        return internalServerError();
    }

    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    public static String generatePassword() {

        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    public static String generateCode(){
        return "";
    }



}
