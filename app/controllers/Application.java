package controllers;

import akka.actor.ActorRef;
import akka.actor.Props;
import bootstrap.MailSenderActor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;

import models.*;
import models.Collection;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import views.html.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.Logger;
import java.text.DecimalFormat;

public class Application extends Controller {

    public static Result preflight(String all) {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Allow", "*");
        response().setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Referer, User-Agent");
        // response().setHeader("Access-Control-Allow-Origin", "https://sandbox.pagseguro.uol.com.br");
        Logger.debug("preflight");
        return ok();
    }

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
        String dateRange = request().getQueryString("dateRange");

        return ok(dashboardTrue.render(dateRange));
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getUsuariosType(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;

        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AggregationResults<DBObject> aggResult = null;
        aggResult = MongoService.getDashboardUsuarioType(startDate,endDate);
        return ok(aggResult.getRawResults().toString());
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getUsuariosGrow(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;

        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AggregationResults<DBObject> aggResult = null;
        aggResult = MongoService.getDashboardUsuarioGrow(startDate,endDate);
        return ok(aggResult.getRawResults().toString());
    }

    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getUsuariosGender(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;

        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AggregationResults<DBObject> aggResult = null;
        aggResult = MongoService.getDashboardUsuarioGender(startDate,endDate);
        return ok(aggResult.getRawResults().toString());
    }


    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getEntryProducts(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;


        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<InventoryEntry> entries = null;
        entries = MongoService.getDashboardEntryProducts(startDate,endDate);
        ObjectNode results = Json.newObject();

        for(InventoryEntry entry : entries){
            String newstring = new SimpleDateFormat("yyyy-MM-dd").format(entry.getCreatedDate());

            if(!results.has(newstring)){
                results.put(newstring,Json.newObject());
            }
            if(!results.with(newstring).has(entry.getInventory().getName())){
                results.with(newstring).put(entry.getInventory().getName(),0);
            }
            if(entry.getQuantity()>0){
                int newValue = results.with(newstring).findValue(entry.getInventory().getName()).intValue()+entry.getQuantity();
                results.with(newstring).put(entry.getInventory().getName(),newValue);
            }
        }
        return ok(results);
    }

    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getLeaveProducts(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;


        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<InventoryEntry> entries = null;
        entries = MongoService.getDashboardEntryProducts(startDate,endDate);
        ObjectNode results = Json.newObject();

        for(InventoryEntry entry : entries){
            String newstring = new SimpleDateFormat("yyyy-MM-dd").format(entry.getCreatedDate());

            if(!results.has(newstring)){
                results.put(newstring,Json.newObject());
            }
            if(!results.with(newstring).has(entry.getInventory().getName())){
                results.with(newstring).put(entry.getInventory().getName(),0);
            }
            if(entry.getQuantity()<0){
                int newValue = Math.abs(results.with(newstring).findValue(entry.getInventory().getName()).intValue()+entry.getQuantity());
                results.with(newstring).put(entry.getInventory().getName(),newValue);
            }
        }
        return ok(results);
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getOrderProductsFaturamento(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;


        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AggregationResults<DBObject> aggResult = null;
        aggResult = MongoService.getDashboardProductsFaturamento(startDate,endDate);
        return ok(aggResult.getRawResults().toString());
    }

    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getOrderProducts(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;


        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AggregationResults<DBObject> aggResult = null;

        aggResult = MongoService.getDashboardProducts(startDate,endDate);
        return ok(aggResult.getRawResults().toString());
    }

    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getOrderSizeCount(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;


        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AggregationResults<DBObject> aggResult = null;

        aggResult = MongoService.getDashboardSoldbySize(startDate,endDate);
        return ok(aggResult.getRawResults().toString());

    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getOrderTypeCount(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;


        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AggregationResults<DBObject> aggResult = null;

        aggResult = MongoService.getDashboardSoldbyType(startDate,endDate);
        return ok(aggResult.getRawResults().toString());

    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getOrderGenderCount(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;


        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AggregationResults<DBObject> aggResult = null;

        aggResult = MongoService.getDashboardSoldbyGender(startDate,endDate);
        return ok(aggResult.getRawResults().toString());

    }

    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getOrderColorCount(){
        String dateRange = request().getQueryString("dateRange");
        DateTime endDate   = null;
        DateTime startDate = null;


        if(dateRange!=null&&!dateRange.equals("")) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            try {
                startDate = formatter.parseDateTime(dateRange.split(" - ")[0]);
                endDate = formatter.parseDateTime(dateRange.split(" - ")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AggregationResults<DBObject> aggResult = null;

        aggResult = MongoService.getDashboardSoldbyColor(startDate,endDate);
        return ok(aggResult.getRawResults().toString());

    }


    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result content(String id){
        SiteContent contentObject = MongoService.findContentById(id);
        contentObject = (contentObject!=null?contentObject:new SiteContent());
        return ok(newContent.render(contentObject));
    }

    /**
     * List
     * */
    @Security.Authenticated(Secured.class)
    public static Result listContent() {
        List<SiteContent> contents = MongoService.getAllContents();
        return ok(listContent.render(contents));
    }

    /**
     * Save
     * */


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
        
        for(Image image:content.getImages()){
            String imageSubTitlesImage = (dataFiles.asFormUrlEncoded().get("subtitle"+image.getName()) != null && dataFiles.asFormUrlEncoded().get("subtitle"+image.getName()).length > 0) ? dataFiles.asFormUrlEncoded().get("subtitle"+image.getName())[0] : null;
            String imageUrlRedirectImage = (dataFiles.asFormUrlEncoded().get("urlRedirect"+image.getName()) != null && dataFiles.asFormUrlEncoded().get("urlRedirect"+image.getName()).length > 0) ? dataFiles.asFormUrlEncoded().get("urlRedirect"+image.getName())[0] : null;
            String promotionImage = (dataFiles.asFormUrlEncoded().get("promotion"+image.getName()) != null && dataFiles.asFormUrlEncoded().get("promotion"+image.getName()).length > 0) ? dataFiles.asFormUrlEncoded().get("promotion"+image.getName())[0] : null;
            image.setSubtitle(imageSubTitlesImage!=null?imageSubTitlesImage:image.getSubtitle());
            image.setRedirectUrl(imageUrlRedirectImage!=null?imageUrlRedirectImage:image.getRedirectUrl());
            image.setPromotion(promotionImage!=null);
        }

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
                if(promotion!=null&&promotion.length > i)
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


    /**
     * Delete
     * */



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



}
