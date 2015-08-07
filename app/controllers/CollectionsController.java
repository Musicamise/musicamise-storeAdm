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

public class CollectionsController extends Controller {

	
	  @Security.Authenticated(Secured.class)
    public static Result listCollections() {
        List<Collection> collections = MongoService.getAllCollections();

        return ok(listCollections.render(collections));
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
            return redirect(routes.CollectionsController.collection(null));
        }

        if(title==null||title.equals("")){
            flash("collection","Insert Title at least");
            return redirect(routes.CollectionsController.collection(null));
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

        return redirect(routes.CollectionsController.listCollections());
    }
   

    @Security.Authenticated(Secured.class)
    public static Result deleteCollection(String id){

        Collection collection = MongoService.findCollectionById(id);
        if(collection!=null){
            MongoService.deleteCollection(collection);
            flash("listCollections","Removed with success");
        }
        return redirect(routes.CollectionsController.listCollections());
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

}