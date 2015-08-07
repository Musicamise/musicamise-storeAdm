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

public class LocalStoreController extends Controller {

	  @Security.Authenticated(Secured.class)
    public static Result listLocalStores() {
        List<LocalStore> localStores = MongoService.getAllLocalStores();

        return ok(listLocalStores.render(localStores));
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
            return redirect(routes.LocalStoreController.localStore(null));
        }

        if(title==null||title.equals("")){
            flash("localStore","Insert Title at least");
            return redirect(routes.LocalStoreController.localStore(null));
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

        if(imagesNew.size() > 0&&localStore.getImages()!=null) {
//            localStore.getImages().deleteFile();
            localStore.setImages(imagesNew);
        }

        MongoService.saveLocalStore(localStore);

        return redirect(routes.LocalStoreController.listLocalStores());
    }
   
     @Security.Authenticated(Secured.class)
    public static Result deleteLocalStore(String id){

        LocalStore localStore = MongoService.findLocalStoreById(id);
        if(localStore!=null){
            MongoService.deleteLocalStore(localStore);
            flash("listLocalStores","Removed with success");
        }
        return redirect(routes.LocalStoreController.listLocalStores());
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
//                    Image image = localStore.getImages();
//                    image.deleteFile();
//                    localStore.setImages(null);
                    MongoService.saveLocalStore(localStore);

                    return ok();
                }catch (Exception e) {
                    return internalServerError();
                }
            }
        }
        return internalServerError();
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
    


}