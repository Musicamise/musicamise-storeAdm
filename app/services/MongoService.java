package services;

import bootstrap.DS;
import models.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
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

        DS.mop.save(user);
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

    public static boolean hasProductById(String id) {

        return   DS.mop.exists(new Query(where("id").is(id)), Product.class);

    }

    public static void deleteUser(User user) {

        DS.mop.remove(user);

    }

    public static void deleteGiftCard(GiftCard giftCard) {

        DS.mop.remove(giftCard);

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

    public static Product findProductByTitle(String textContent) {
        Product product = DS.mop.findOne(new Query(where("title").is(textContent)),Product.class);
        return product;
    }

    public static void saveOrder(List<Order> ordersNew) {
        for (Order order : ordersNew) {
            DS.mop.save(order);
        }
    }

    public static Order findOrdersById(String id) {
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

    public static boolean hasDiscountCodeByCode(String code) {
        return DS.mop.exists(new Query(where("code").is(code)), DiscountCode.class);
    }

    public static void saveDiscountCode(DiscountCode discount) {
        DS.mop.save(discount);
    }

    public static List<Collection> getAllCollections() {
        return DS.mop.findAll(Collection.class);
    }

    public static boolean createInitialColletions(){
        try {


            for (Utils.CollectionType collectionType : Utils.CollectionType.values()) {
                Collection collection = new Collection();
                collection.setTitle(collectionType.title);
                collection.setSlug(collectionType.name());
                if(!hasCollectionBySlug(collection.getSlug())) {
                    saveCollection(collection);
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

    public static boolean hasCollectionBySlug(String slug) {
        return DS.mop.exists(new Query(where("slug").is(slug)), Collection.class);
    }
    public static boolean hasCollectionById(String id) {
        return DS.mop.exists(new Query(where("_id").is(id)), Collection.class);
    }

    public static boolean saveCollection(Collection collection){
        DS.mop.save(collection);
        return true;
    }

    public static List<Product> findProductByCollectionSlug(List<String> collectionsSlugs) {

        Query query = new Query();
        query.addCriteria(Criteria.where("collectionsSlugs").elemMatch(Criteria.where(null).in(collectionsSlugs)));

        return DS.mop.find(query,Product.class);
    }

    public static List<Product> findProductByCollectionSlugOrListId(List<String> collectionsSlugs,List<String> productsList) {

        Query query = new Query();
        query.addCriteria(Criteria.where(null).orOperator(Criteria.where("collectionsSlugs").elemMatch(Criteria.where(null).in(collectionsSlugs)),
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

    public static boolean hasInventoryByProductAndSize(String productId,String size){
        Query query = new Query();
        query.addCriteria(Criteria.where(null).andOperator(Criteria.where("product.id").is(productId),
                Criteria.where("size").is(size)));


        boolean has =  DS.mop.exists(query,Inventory.class);

        return has;
    }
}
