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

public class GiftCardController extends Controller {

	@AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result giftCard(String id){
        GiftCard giftCard = MongoService.findGiftCardById(id);
        giftCard= (giftCard!=null?giftCard:new GiftCard());
        List<User> users = MongoService.getAllUsers();

        return ok(newGiftCode.render(giftCard,users));
    }
	
@Security.Authenticated(Secured.class)
    public static Result listGiftCard() {
        List<GiftCard> giftCards = MongoService.getAllGiftCards();
        return ok(listGiftCard.render(giftCards));
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

            return redirect(routes.GiftCardController.giftCard(null));
        }

        if(code==null||code.equals("")){
            flash("giftCard","Insert a Valid Code");
            return redirect(routes.GiftCardController.giftCard(null));
        }
        if(id==null&&MongoService.hasGiftCardById(code)){
            flash("giftCard","Insert a Valid Code");
            return redirect(routes.GiftCardController.giftCard(null));
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
        return redirect(routes.GiftCardController.listGiftCard());
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteGiftCard(String id){
        GiftCard giftCard = MongoService.findGiftCardById(id);
        if(giftCard!=null){
            MongoService.deleteGiftCard(giftCard);
            flash("listGiftCard","Removed with success");
        }
        return redirect(routes.GiftCardController.listGiftCard());
    }

}
