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

public class ProductController extends Controller {



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

    @Security.Authenticated(Secured.class)
    public static Result listProduct() {
        List<Product> products = MongoService.getAllProducts();
        return ok(listProduct.render(products));
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
            return redirect(routes.ProductController.product(null));
        }

        if(title==null||title.equals("")){
            flash("product","Insert Title at least");
            return redirect(routes.ProductController.product(null));
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

        for(Image image:product.getImages()){
            String frontImage = (dataFiles.asFormUrlEncoded().get("frontImage"+image.getName()) != null && dataFiles.asFormUrlEncoded().get("frontImage"+image.getName()).length > 0) ? dataFiles.asFormUrlEncoded().get("frontImage"+image.getName())[0] : null;
            image.setFrontImage(frontImage!=null);
        }

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

        return redirect(routes.ProductController.listProduct());
    }

        @Security.Authenticated(Secured.class)
    public static Result deleteProduct(String id){
        Product product = MongoService.findProductById(id);
        if(product!=null){
            MongoService.deleteProduct(product);
            flash("listProduct","Removed with success");
        }
        return redirect(routes.ProductController.listProduct());
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

}