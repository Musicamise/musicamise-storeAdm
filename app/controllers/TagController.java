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

public class TagController extends Controller {


	 @Security.Authenticated(Secured.class)
    public static Result deleteTag(String id){

        Tag tag = MongoService.findTagById(id);
        if(tag!=null){
            MongoService.deleteTag(tag);
            flash("listTags","Removed with success");
        }
        return redirect(routes.TagController.listTags());
    }

    @Security.Authenticated(Secured.class)
    public static Result listTags() {
        List<Tag> tags = MongoService.getAllTags();

        return ok(listTags.render(tags));
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
            return redirect(routes.TagController.tag(null));
        }

        if(title==null||title.equals("")){
            flash("tag","Insert Title at least");
            return redirect(routes.TagController.tag(null));
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

        return redirect(routes.TagController.listTags());
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


}