package services;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import play.Logger;

import bootstrap.DS;
import models.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by alvaro on 25/08/2014.
 */
public class MongoService {

    public static List<User> getAllUsers(){

        List<User> users = DS.mop.findAll(User.class);
        return users;
    }
    public static List<User> getAllUsers(String filter, String queryString, String team){

        List<User> users;
        Query query = new Query();

        if(queryString!=null&&!queryString.equals("")) {
            query.addCriteria(Criteria.where(filter).regex(queryString,"i"));
        }

        if(team!=null&&!team.equals("")){
            query.addCriteria(Criteria.where("timePreferido").is(team));
        }

        users = DS.mop.find(query,User.class);


        return users;
    }
    public static boolean saveUser(User user){
        try{
           DS.mop.save(user);
        }catch(Exception e){
            return false;
        }
        return true;
    }

    public static User findUserByEmail(String email) {
        User user = null;
        if(email!=null)
            user = DS.mop.findOne(new Query(where("email").is(email)), User.class);

        return user;
    }
    public static boolean hasUserByEmail(String email) {

        return   DS.mop.exists(new Query(where("email").is(email)), User.class);

    }

    public static boolean hasUserById(String id) {

        return   DS.mop.exists(new Query(where("_id").is(id)), User.class);

    }

    public static boolean hasProductById(String id) {

        return   DS.mop.exists(new Query(where("id").is(id)), Product.class);

    }

    public static void deleteUser(User user) {

        DS.mop.remove(user);

    }

    public static void deleteGiftCard(GiftCard giftCard) {

        DS.mop.remove(giftCard);

    }
    public static void deleteTag(Tag tag) {
        List<String> tagList = new ArrayList<>();
        tagList.add(tag.getSlug());
        List<Product> products = findProductByTagsSlug(tagList);
        products.stream().forEach(p->p.getUserTags().remove(tag.getSlug()));
        saveProducts(products);
        DS.mop.remove(tag);

    }
    public static void deleteLocalStore(LocalStore localstore) {
        List<String> localStoreList = new ArrayList<>();
        localStoreList.add(localstore.getSlug());
        List<Product> products = findProductByLocalStoreSlug(localStoreList);
        products.stream().forEach(p->p.getLocalStoresSlugs().remove(localstore.getSlug()));
        saveProducts(products);
        DS.mop.remove(localstore);

    }
    public static void deleteCollection(Collection collection) {
        List<String> collectionList = new ArrayList<>();
        collectionList.add(collection.getSlug());
        List<Product> products = findProductByCollectionSlug(collectionList);
        products.stream().forEach(p->p.getUserTags().remove(collection.getSlug()));
        saveProducts(products);
        DS.mop.remove(collection);

    }
    public static void deleteDiscountCode(DiscountCode discountCode) {

        DS.mop.remove(discountCode);

    }


   

    public static void saveProduct(Product product) {
        DS.mop.save(product);
    }

    public static List<Product> findProductsByType(Utils.ProductType type) {
        List<Product> products = DS.mop.find(new Query(where("type").is(type)), Product.class);

        return (products!=null)?products:new ArrayList<Product>();
    }

    public static void deleteProduct(Product product) {
        DS.mop.remove(product);
    }


    public static void saveProducts(List<Product> products) {
        for (Product product : products) {
            DS.mop.save(product);
        }

    }

    public static void saveOrder(Order order) {
        DS.mop.save(order);
    }

    public static Product findProductById(String id) {
        return DS.mop.findById(id,Product.class);
    }

    public static List<Order> findOrdersByIdCompra(String code) {
        return DS.mop.find(new Query(where("idCompra").is(code)), Order.class);
    }

    public static boolean hasOrderById(String id){
        return   DS.mop.exists(new Query(where("_id").is(id)), Order.class);
    }

    public static Product findProductByTitle(String textContent) {
        Product product = DS.mop.findOne(new Query(where("title").is(textContent)),Product.class);
        return product;
    }

    public static void saveOrder(List<Order> ordersNew) {
        for (Order order : ordersNew) {
            DS.mop.save(order);
        }
    }

    public static Order findOrderById(String id) {
        Order order = DS.mop.findById(id,Order.class);
        return order;
    }

    public static int countProductWithSlug(String slug) {
        Query query = new Query();
        query.addCriteria(Criteria.where("slug").regex(slug,"i"));

        return (int)DS.mop.count(query,Product.class);
    }

    public static Product findProductBySlug(String slug) {
        return DS.mop.findOne(new Query(where("slug").is(slug)),Product.class);
    }

    public static List<Product> getAllProducts() {
        List<Product> products = DS.mop.findAll(Product.class);

        return products;
    }


    public static List<Inventory> getAllInventories() {
        return DS.mop.findAll(Inventory.class);
    }

    public static List<Inventory> findInventoriesByIds(List<String> ids) {
        List<ObjectId> idList = new ArrayList<>();
        for(String id:ids){
            idList.add(new ObjectId(id));
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").in(idList));

        return DS.mop.find(query,Inventory.class);
    }

    public static Inventory findInventoryById(String id) {

        return DS.mop.findById(id,Inventory.class);
    }


    public static void deleteInventory(Inventory inventory) {
            DS.mop.remove(inventory);
    }

    public static boolean hasInventoryById(String id){
        return   DS.mop.exists(new Query(where("_id").is(id)), Inventory.class);
    }

    public static void saveInventory(Inventory inventory) {
        DS.mop.save(inventory);
    }
    public static void saveInventories(List<Inventory> inventories) {
        for(Inventory inventory :inventories){
            MongoService.saveInventory(inventory);
        }
    }

    public static void saveInventoryEntry(InventoryEntry inventoryEntry) {
        DS.mop.save(inventoryEntry);
    }
    public static GiftCard findGiftCardById(String id) {
        GiftCard giftCard = DS.mop.findById(id,GiftCard.class);
        return giftCard;

    }

    public static List<GiftCard> getAllGiftCards() {
        return DS.mop.findAll(GiftCard.class);
    }

    public static boolean hasGiftCardById(String id){
        return   DS.mop.exists(new Query(where("_id").is(id)), GiftCard.class);
    }


    public static void saveGiftCard(GiftCard giftCard) {
        DS.mop.save(giftCard);
    }

    public static List<DiscountCode> getAllDiscountCodes() {
        return DS.mop.findAll(DiscountCode.class);
    }

    public static DiscountCode findDiscountCodeById(String id) {
        DiscountCode discountCode = DS.mop.findById(id,DiscountCode.class);

        return discountCode;
    }

    public static List<DiscountCode> findDiscountCodeByProduct(Product product) {
        Query query = new Query();
        List<String> productSlugs = new ArrayList<>();
        if(product.getSlug()!=null)
            productSlugs.add(product.getSlug());
        query.addCriteria(Criteria.where(null).orOperator(
                            Criteria.where("collectionsSlug").elemMatch(Criteria.where(null).in(product.getCollectionsSlugs())),
                            Criteria.where("productSlugs").in(productSlugs),
                            Criteria.where("ordersValidation").is(Utils.DiscountValidation.all.name())
                            ));
        Logger.debug(query.toString());
        return DS.mop.find(query,DiscountCode.class);
    }

    public static List<DiscountCode> findDiscountCodesByActive() {
        Query query = new Query();
        query.addCriteria( Criteria.where("active").is(true)
                            );
        return DS.mop.find(query,DiscountCode.class);
    }

    public static boolean hasDiscountCodeByCode(String code) {
        return DS.mop.exists(new Query(where("code").is(code)), DiscountCode.class);
    }

    public static void saveDiscountCode(DiscountCode discount) {
        DS.mop.save(discount);
    }

    public static List<Collection> getAllCollections() {
        return DS.mop.findAll(Collection.class);
    }
    public static List<Tag> getAllTags() {
        return DS.mop.findAll(Tag.class);
    }
     public static List<LocalStore> getAllLocalStores() {
        return DS.mop.findAll(LocalStore.class);
    }

    public static boolean createInitialColletions(){
        try {


            for (Utils.CollectionType collectionType : Utils.CollectionType.values()) {
                Collection collection = new Collection();
                collection.setTitle(collectionType.title);
                if(!hasCollectionBySlug(collection.getSlug())) {
                    saveCollection(collection);
                }
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }
    public static boolean createInitialTags(){
        try {

            for (Utils.UserTags tagType : Utils.UserTags.values()) {

                Tag tag = new Tag();
                tag.setTitle(tagType.title);
                if(!hasTagBySlug(tag.getSlug())) {
                    saveTag(tag);
                }
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public static Collection findCollectionById(String id) {
        return DS.mop.findById(id,Collection.class);
    }
    public static List<Collection> findCollectionByGender() {

        return DS.mop.find(new Query(where("gender").is(true)),Collection.class);
    }
    

    public static boolean hasCollectionBySlug(String slug) {
        return DS.mop.exists(new Query(where("slug").is(slug)), Collection.class);
    }

    public static boolean hasCollectionByGender(String gender) {
        Query query = new Query();
        query.addCriteria(Criteria.where(null)
                .andOperator(
                    Criteria.where("slug").is(gender),
                    Criteria.where("gender").is(true) ));

        return DS.mop.exists(query, Collection.class);
    }

    public static boolean hasCollectionById(String id) {
        return DS.mop.exists(new Query(where("_id").is(id)), Collection.class);
    }

    public static boolean saveCollection(Collection collection){
        DS.mop.save(collection);
        return true;
    }
    public static Tag findTagById(String id) {
        return DS.mop.findById(id,Tag.class);
    }
    public static LocalStore findLocalStoreById(String id) {
        return DS.mop.findById(id,LocalStore.class);
    }

    public static boolean hasTagBySlug(String slug) {
        return DS.mop.exists(new Query(where("slug").is(slug)), Tag.class);
    }
    public static boolean hasTagById(String id) {
        return DS.mop.exists(new Query(where("_id").is(id)), Tag.class);
    }

    public static boolean hasLocalStoreBySlug(String slug) {
        return DS.mop.exists(new Query(where("slug").is(slug)), LocalStore.class);
    }
    public static boolean hasLocalStoreById(String id) {
        return DS.mop.exists(new Query(where("_id").is(id)), LocalStore.class);
    }

    public static boolean saveTag(Tag tag){
        DS.mop.save(tag);
        return true;
    }
    public static boolean saveLocalStore(LocalStore localstore){
        DS.mop.save(localstore);
        return true;
    }

    public static List<Product> findProductByCollectionSlug(List<String> collectionsSlugs) {

        Query query = new Query();
        query.addCriteria(Criteria.where("collectionsSlugs").elemMatch(Criteria.where(null).in(collectionsSlugs)));

        return DS.mop.find(query,Product.class);
    }
    public static List<Product> findProductByTagsSlug(List<String> tagsSlugs) {

        Query query = new Query();
        query.addCriteria(Criteria.where("userTags").elemMatch(Criteria.where(null).in(tagsSlugs)));

        return DS.mop.find(query,Product.class);
    }
    public static List<Product> findProductByLocalStoreSlug(List<String> localStoresSlugs) {

        Query query = new Query();
        query.addCriteria(Criteria.where("localStoresSlugs").elemMatch(Criteria.where(null).in(localStoresSlugs)));

        return DS.mop.find(query,Product.class);
    }

    public static List<Product> findProductByCollectionSlugOrListId(List<String> collectionsSlugs,List<String> productsList) {

        Query query = new Query();
        query.addCriteria(Criteria.where(null).orOperator(Criteria.where("collectionsSlugs").elemMatch(Criteria.where(null).in(collectionsSlugs)),
                         Criteria.where("_id").in(productsList)));

        return DS.mop.find(query,Product.class);
    }
    public static List<Product> findProductByTagSlugOrListId(List<String> tagsSlugs,List<String> productsList) {

        Query query = new Query();
        query.addCriteria(Criteria.where(null).orOperator(Criteria.where("userTags").elemMatch(Criteria.where(null).in(tagsSlugs)),
                         Criteria.where("_id").in(productsList)));

        return DS.mop.find(query,Product.class);
    }
    public static List<Product> findProductByLocalStoreSlugOrListId(List<String> localStoresSlugs,List<String> productsList) {

        Query query = new Query();
        query.addCriteria(Criteria.where(null).orOperator(Criteria.where("localStoresSlugs").elemMatch(Criteria.where(null).in(localStoresSlugs)),
                         Criteria.where("_id").in(productsList)));

        return DS.mop.find(query,Product.class);
    }

    public static List<Product> findProductsByIds(List<String> productsList) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").in(productsList))   ;

        return DS.mop.find(query,Product.class);
    }

    public static User findUserById(String id) {
        return DS.mop.findById(id,User.class);
    }

    public static List<Order> getAllOrders() {
        return DS.mop.findAll(Order.class);
    }

    public static boolean hasInventoryByProductIdSizeAndGender(String productId,String size,String gender){
        Query query = new Query();
        query.addCriteria(Criteria.where(null)
                .andOperator(
                    Criteria.where("product.id").is(productId),
                    Criteria.where("size").is(size),
                    Criteria.where("genderSlug").is(gender)));


        boolean has =  DS.mop.exists(query,Inventory.class);

        return has;
    }


    public static boolean saveContent(SiteContent content){
        DS.mop.save(content);
        return true;
    }
    public static List<SiteContent> getAllContents(){
        List<SiteContent> contents = DS.mop.findAll(SiteContent.class);
        return contents;
    }
    public static SiteContent findContentById(String id) {
        return DS.mop.findById(id,SiteContent.class);
    }
    public static boolean createInitialContent(){
        try {

            for (Utils.SiteContent contentType : Utils.SiteContent.values()) {

                SiteContent content = new SiteContent();
                content.setType(contentType);
                if(!hasContentById(content.getType())) {
                    saveContent(content);
                }
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }
    public static boolean hasContentById(String id) {
        return DS.mop.exists(new Query(where("_id").is(id)), SiteContent.class);
    }
    public static boolean hasContentById(Utils.SiteContent id) {
        return DS.mop.exists(new Query(where("_id").is(id.name())), SiteContent.class);
    }




    //dashboard


    //  var reduce = function(inventoryKey, quantities) {
    //                           return Array.sum(quantities);
    //                       };

    // var outTable = {out:'teste1'}

    // db.order.mapReduce(mapQuantity,reduce,outTable);

       public static MapReduceResults<ValueObject> getOrdersInventoryByQuantity(boolean thisMonth, Utils.DashBoardQueryType type){
        String collection = "order";
        Query query = new Query();
        if(thisMonth){
            DateTime now = new DateTime();
            query.addCriteria(where("createdDate").gte(now.minusMonths(1)).lte(now));
        }
        String map ;
        switch (type){
            case gender: map = "function() {\n" +
                    "     for(var i = 0;i<this.products.length;i++){\n" +
                    "         emit( this.products[i].genderSlug , this.products[i].quantity );\n" +
                    "     }\n" +
                    "};";
                break;
            case inventoryId: map = "function() {\n" +
                    "     for(var i = 0;i<this.products.length;i++){\n" +
                    "         emit( this.products[i]._id , this.products[i].quantity ); \n" +
                    "     }\n" +
                    "};";
                break;
            case size: map = "function() {\n" +
                    "     for(var i = 0;i<this.products.length;i++){\n" +
                    "         emit( this.products[i].size , this.products[i].quantity );\n" +
                    "     }\n" +
                    "};";
                break;
            default: map = "function() {\n" +
                    "     for(var i = 0;i<this.products.length;i++){\n" +
                    "         emit( this.products[i]._id , this.products[i].quantity ); // quantity\n" +
                    "     }\n" +
                    "};";
                break;
        }


        String reduce = "function(inventoryKey, quantities) {\n" +
                "    return Array.sum(quantities);\n" +
                "};";

        MapReduceResults<ValueObject> results =  DS.mop.mapReduce(query,collection,map, reduce,ValueObject.class);
        return results;
    }

    private static List<Order> getOrdersInventoryByDate(DateTime startDate,DateTime endDate){
        Query query = new Query();
        if(startDate!=null&&endDate!=null){
            query.addCriteria(where("createdDate").gte(startDate).lte(endDate));
        }else if(startDate==null&&endDate!=null){
            query.addCriteria(where("createdDate").lte(endDate));

        }else if(startDate!=null&&endDate==null){
            query.addCriteria(where("createdDate").gte(startDate));
        }
        List<Order> results =  DS.mop.find(query,Order.class);
        return results;
    }

    public static void getDashboard(DateTime startDate, DateTime endDate,AggregationResults<DBObject> aggResultsProducts,AggregationResults<DBObject> aggResultsSize,
        AggregationResults<DBObject> aggResultsGender,AggregationResults<DBObject> aggResultsEntry,AggregationResults<DBObject> aggResultsOrder,
        AggregationResults<DBObject> aggResultsOrderPrice,List<Order> ordersByDate){


        //Products sold
        BasicDBObject obj = new BasicDBObject();
        obj.append("date","$createdDate");
        obj.append("quantity","$products.quantity");
        obj.append("gender","$products.genderSlug");
        obj.append("size","$products.size");
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdDate").gte(startDate).lte(endDate)),
                Aggregation.unwind("products"),
                Aggregation.group("products.product").push(obj).as("data")
        );
        aggResultsProducts = DS.mop.aggregate(agg,Order.class, DBObject.class);

        //Products sold by size
        obj = new BasicDBObject();
        obj.append("date","$createdDate");
        obj.append("quantity","$products.quantity");
          agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdDate").gte(startDate).lte(endDate)),
                Aggregation.unwind("products"),
                Aggregation.group("products.size").push(obj).as("data")
        );
        aggResultsSize = DS.mop.aggregate(agg,Order.class, DBObject.class);

        //Products sold by gender
        obj = new BasicDBObject();
        obj.append("date","$createdDate");
        obj.append("quantity","$products.quantity");
        agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdDate").gte(startDate).lte(endDate)),
                Aggregation.unwind("products"),
                Aggregation.group("products.genderSlug").push(obj).as("data")
        );
        aggResultsGender = DS.mop.aggregate(agg,Order.class, DBObject.class);

        //General Entry
        obj = new BasicDBObject();
        obj.append("date","$createdDate");
        obj.append("quantity","$quantity");
        agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdDate").gte(startDate).lte(endDate)),
                Aggregation.group("$inventory").push(obj).as("data").sum("$quantity").as("total")
        );
        aggResultsEntry = DS.mop.aggregate(agg,InventoryEntry.class, DBObject.class);

        //General Orders

        obj = new BasicDBObject();
        obj.append("date","$createdDate");
        obj.append("quantity","$products.quantity");

        agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdDate").gte(startDate).lte(endDate)),
                Aggregation.unwind("$products"),
                Aggregation.group("$products.sku").push(obj).as("data").sum("$products.quantity").as("total")
        );
        aggResultsOrder = DS.mop.aggregate(agg,Order.class, DBObject.class);


        obj = new BasicDBObject();
        obj.append("date","$createdDate");
        obj.append("total","$total");
        obj.append("products","$products");

        agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdDate").gte(startDate).lte(endDate)),
                Aggregation.group("$id").push(obj).as("data")
        );

        aggResultsOrderPrice = DS.mop.aggregate(agg,Order.class, DBObject.class);

        Query query = new Query();
        query.addCriteria(Criteria.where("createdDate").gte(startDate).lte(endDate));
        ordersByDate = DS.mop.find(query,Order.class);

    }

    public static AggregationResults<DBObject> getDashboardProducts(DateTime startDate, DateTime endDate){


        //Products sold
        BasicDBObject obj = new BasicDBObject();
        obj.append("date","$createdDate");
        obj.append("quantity","$products.quantity");
        obj.append("gender","$products.genderSlug");
        obj.append("size","$products.size");
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdDate").gte(startDate).lte(endDate)),
                Aggregation.unwind("products"),
                Aggregation.group("products.product").push(obj).as("data")
        );
        return DS.mop.aggregate(agg,Order.class, DBObject.class);

    }



}
