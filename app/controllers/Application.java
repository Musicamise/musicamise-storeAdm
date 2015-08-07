package controllers;

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
import java.text.ParseException;
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
    
    public static Result updatecompra()  {

        Logger.debug("hit");
        if(request().hasHeader("Origin")&&!request().getHeader("Origin").contains("pagseguro")){
            Logger.debug("hit");
            return badRequest();
        }
        Map<String, String[]> data = request().body().asFormUrlEncoded();
        if(data!=null){
            
            String urlWs = Play.application().configuration().getString("pagseguro.ws.notification.url");
            String emailPagseguro = null;
            String token = null;
            if(System.getenv("pagseguro.email")!=null){
                emailPagseguro = System.getenv("pagseguro.email");
            }else{
                emailPagseguro =  Play.application().configuration().getString("pagseguro.email");
            }
            if(System.getenv("pagseguro.token")!=null){
                token = System.getenv("pagseguro.token");
            }else{
                token =  Play.application().configuration().getString("pagseguro.token");
            }

            String notificationCode = (data.get("notificationCode") != null && data.get("notificationCode").length > 0) ? data.get("notificationCode")[0] : "";

            String notificationType = (data.get("notificationType") != null && data.get("notificationType").length > 0) ? data.get("notificationType")[0] : "";

            WSRequestHolder holder = WS.url(urlWs + notificationCode);

            WSResponse response =   holder.setQueryParameter("email", emailPagseguro)
                    .setQueryParameter("token", token)
                    .get()
                    .get(10000);

            Document respostaDoc = response.asXml();
            Logger.debug(respostaDoc.toString());
            NodeList sender = respostaDoc.getElementsByTagName("sender");
            NodeList items = respostaDoc.getElementsByTagName("item");
            NodeList codeTag = respostaDoc.getElementsByTagName("code");
            NodeList referenceTag = respostaDoc.getElementsByTagName("reference");

            NodeList statusTag = respostaDoc.getElementsByTagName("status");


            String code = codeTag.item(0).getTextContent();
            String status = statusTag.item(0).getTextContent();
            String reference = referenceTag.item(0).getTextContent();

            
            Logger.debug("code");
            Logger.debug(code);
            Logger.debug("status Code");
            Logger.debug(status);
            Logger.debug(Utils.StatusCompra.getStatusByCode(Integer.parseInt(status)).name());

            Order order = MongoService.findOrderById(reference);
            Logger.debug("order");
            if(order!=null){
                
                Logger.debug("status From order");
                Logger.debug(order.getStatus().toString());
                StatusOrder newStatus = new StatusOrder(Utils.StatusCompra.getStatusByCode(Integer.parseInt("3")));
                MongoService.upDateOrder(reference,newStatus,code);
            }else{
                return notFound();
            }

        }


        return ok();
    }
    

    public static String generateCode(){
        return "";
    }



}
