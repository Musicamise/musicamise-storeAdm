package bootstrap; /**
 * Created by Alvaro on 18/03/2015.
 */
import akka.actor.ActorRef;
import akka.actor.Props;
import models.*;
import play.*;
import play.libs.Akka;
import play.libs.F.*;
import play.mvc.*;
import play.mvc.Http.*;
import services.MongoService;

import java.util.*;

import static play.mvc.Results.*;

public class Global extends GlobalSettings {
    //removed for pagseguro 
    //  @Override
    // public <T extends EssentialFilter> Class<T>[] filters() {
    //     Logger.debug("filter CSRF");
    //     Logger.debug(this.toString());
    //     return new Class[]{CSRFFilter.class};
    // }

    public void onStart(Application app) {
        DS.init();

        MongoService.createInitialColletions();
        MongoService.createInitialTags();
        MongoService.createInitialContent();

        createUser();
        createProducts();
        Logger.info("Application has started");
    }
    public static void createUser(){
        String password = System.getenv("ADMIN_PASSWORD")!=null?System.getenv("ADMIN_PASSWORD"):"admin";
        User user = new User("Admin", "administrador@musicamise.com.br", password);
        user.setManager(true);
        MongoService.saveUser(user);
    }
    public static void createProducts(){
       /* for(int i = 0 ;i<50; i++){
            saveProduct(i); 
        }
        for(int i = 0 ;i<50; i++){
            saveInventory(i); 
        }*/
    }
    public static void saveProduct(int id){

        String title = "title"+id;
        String description = "description"+id;
        String price = "4"+id;
        String priceCompareWith = "0";
        String online = "online"+id;
        String canbuy = "canbuy"+id;
        String store = "store"+id;
        String newProduct = "newProduct"+id;
        String weight = ""+id;
        String mail = "mail"+id;
        String[] tags = {"tags"+id} ;
        String[] productType = {"productType"+id} ;
        String[] collections ={"collections"+id} ; 


        String[] localStores = {"localStores"+id};


        boolean onlineBool = (online!=null)?true:false;
        boolean canbuyBool = (canbuy!=null)?true:false;
        boolean storeBool = (store!=null)?true:false;
        boolean newProductBool = (newProduct!=null)?true:false;
        boolean mailBool = (mail!=null)?true:false;

        double priceDouble = Double.parseDouble(price);
        double priceCompareWithDouble = Double.parseDouble(priceCompareWith);
        double weightDouble = Double.parseDouble(weight);

        Set<String> tagsList =  (tags!=null)?new HashSet<>(Arrays.asList(tags)):new HashSet<>();
        Set<String> collectionsList =  (collections!=null)?new HashSet<>(Arrays.asList(collections)):new HashSet<>();
        Set<String> productTypeList =  (productType!=null)?new HashSet<>(Arrays.asList(productType)):new HashSet<>();

        Set<String> localStoresList =  (localStores!=null)?new HashSet<>(Arrays.asList(localStores)):new HashSet<>();


        //build product object
        Product product = new Product();

        product.setTitle(title);
        product.setDescription(description);
        product.setNewProduct(newProductBool);
        product.setOnLineVisible(onlineBool);
        product.setCanBuy(canbuyBool);
        product.setPrice(priceDouble);
        product.setPriceCompareWith(priceCompareWithDouble);
        product.setSendMail(mailBool);
        product.setStoreVisible(storeBool);
        product.setTypes(productTypeList);
        product.setUserTags(tagsList);
        product.setWeight(weightDouble);
        product.setCollectionsSlugs(collectionsList);
        product.setLocalStoresSlugs(localStoresList);


        // product.setHasDiscount(hasDiscountBool);
        // product.setDiscount(valueOfDiscountDouble);
        // product.setDiscountType(typeDiscount);


        List<Image> imagesNew = new ArrayList<>();

        Image image = new Image();
        String imageName = "voltei2_estampa";

        image.setName(imageName);
        image.setUrl("https://s3-sa-east-1.amazonaws.com/musicamise/voltei2_estampa.jpg");
        imagesNew.add(image);

        product.getImages().addAll(imagesNew);
        MongoService.saveProduct(product);

    }
    public static void saveInventory(int id){

        String outOfStock = null;
        String productSize = "M";
        String quantity = "10";
        String sellInOutOfStock = null;
        String gender = "masculino";
        String productType = "Camiseta";
        String color =  "rgb(255, 187, "+id+")";

        boolean outOfStockBool = (outOfStock!=null)?true:false;
        boolean sellInOutOfStockBool = (sellInOutOfStock!=null)?true:false;


        int quantityInt = Integer.parseInt(quantity.trim().replace(".",""));

        //Validation
        Product product = MongoService.findProductByTitle("title"+id);

        Inventory inventory = new Inventory();
        inventory.setSku("sku"+(MongoService.countAllInventories()+1));

        inventory.setOrderOutOfStock(outOfStockBool);
        inventory.setProduct(product);
        inventory.setQuantity(quantityInt);
        inventory.setSize(productSize);
        inventory.setSellInOutOfStock(sellInOutOfStockBool);
        inventory.setGenderSlug(gender);
        inventory.setType(productType);
        inventory.setColor(color);

        //save Inventory
        MongoService.saveInventory(inventory);
        // Save Inventory Entry
        
        //product update
        product.getInventories().add(inventory);
        MongoService.saveProduct(product);

    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }

    public Promise<Result> onError(RequestHeader request, Throwable t) {
        return Promise.<Result>pure(internalServerError(
                //views.html.static_error.render(t)
                views.html.static_error.render()

        ));
    }

    public Promise<Result> onHandlerNotFound(RequestHeader request) {
        return Promise.<Result>pure(notFound(
                // views.html.static_notFound.render(request.uri())
                views.html.static_notFound.render()

        ));
    }
    public Promise<Result> onBadRequest(RequestHeader request, String error) {
        return Promise.<Result>pure(badRequest("Don't try to hack the URI!"));
    }

    // private class ActionWrapper extends Action.Simple {
    //     public ActionWrapper(Action<?> action) {
    //         this.delegate = action;
    //     }

    //     @Override
    //     public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
    //         Promise<Result> result = this.delegate.call(ctx);
    //         Http.Response response = ctx.response();
    //         response.setHeader("Access-Control-Allow-Origin", "*");
    //         return result;
    //     }
    // }

    // @Override
    // public Action<?> onRequest(Http.Request request,
    //                            java.lang.reflect.Method actionMethod) {
    //     return new ActionWrapper(super.onRequest(request, actionMethod));
    // }



}