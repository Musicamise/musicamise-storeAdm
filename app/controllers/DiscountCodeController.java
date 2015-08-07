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

public class DiscountCodeController extends Controller {

	 @Security.Authenticated(Secured.class)
    public static Result listDiscount() {
        List<DiscountCode> discounts = MongoService.getAllDiscountCodes();

        return ok(listDiscountCode.render(discounts));
    }

      @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveDiscountCode(String id){

        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();

        String code = (dataFiles.get("code") != null && dataFiles.get("code").length > 0) ? dataFiles.get("code")[0] : null;
        String timesLeft = (dataFiles.get("timesLeft") != null && dataFiles.get("timesLeft").length > 0) ? dataFiles.get("timesLeft")[0] : null;
        String noLimits = (dataFiles.get("noLimits") != null && dataFiles.get("noLimits").length > 0) ? dataFiles.get("noLimits")[0] : null;
        String discountType = (dataFiles.get("discountType") != null && dataFiles.get("discountType").length > 0) ? dataFiles.get("discountType")[0] : null;
        String valueOf = (dataFiles.get("valueOF") != null && dataFiles.get("valueOF").length > 0) ? dataFiles.get("valueOF")[0] : null;
        String validationType = (dataFiles.get("validationType") != null && dataFiles.get("validationType").length > 0) ? dataFiles.get("validationType")[0] : null;
        String applyCondition = (dataFiles.get("applyCondition") != null && dataFiles.get("applyCondition").length > 0) ? dataFiles.get("applyCondition")[0] : null;
        String noDateLimite = (dataFiles.get("noDateLimite") != null && dataFiles.get("noDateLimite").length > 0) ? dataFiles.get("noDateLimite")[0] : null;
        String dateRange = (dataFiles.get("dateRange") != null && dataFiles.get("dateRange").length > 0) ? dataFiles.get("dateRange")[0] : null;
        String overValueInput = (dataFiles.get("overValue") != null && dataFiles.get("overValue").length > 0) ? dataFiles.get("overValue")[0] : null;

        String active = (dataFiles.get("active") != null && dataFiles.get("active").length > 0) ? dataFiles.get("active")[0] : null;

        String[] collections = (dataFiles.get("collections") != null && dataFiles.get("collections").length > 0) ? dataFiles.get("collections") : null;
        String[] products = (dataFiles.get("products") != null && dataFiles.get("products").length > 0) ? dataFiles.get("products") : null;
        Set<String> collectionList =  (collections!=null)?new HashSet<>(Arrays.asList(collections)):new HashSet<>();
        Set<String> productsnList =  (products!=null)?new HashSet<>(Arrays.asList(products)):new HashSet<>();

        boolean noLimitsBool = (noLimits!=null)?true:false;
        boolean noDateLimiteBool = (noDateLimite!=null)?true:false;
        boolean activeBool = (active!=null)?true:false;


        Utils.DiscountValidation validation = Utils.DiscountValidation.valueOf(validationType);
        Utils.DiscountPaymentAdjust paymentAdjust = Utils.DiscountPaymentAdjust.valueOf(applyCondition);
        Utils.DiscountType typeDiscount = Utils.DiscountType.valueOf(discountType);

        String onLocalStore = (dataFiles.get("onLocalStore") != null && dataFiles.get("onLocalStore").length > 0) ? dataFiles.get("onLocalStore")[0] : null;
        String onLineVisible = (dataFiles.get("onLineVisible") != null && dataFiles.get("onLineVisible").length > 0) ? dataFiles.get("onLineVisible")[0] : null;

        boolean onLocalStoreBool = (onLocalStore!=null)?true:false;
        boolean onLineVisibleBool = (onLineVisible!=null)?true:false;




        double value = 0;
        try {
            value = Double.parseDouble((valueOf != null && !valueOf.equals("")) ? valueOf : "0");
        }catch (Exception e){

        }
        double overValueInputDouble = 0;
        try {

            overValueInputDouble = Double.parseDouble((overValueInput!=null&&!overValueInput.equals(""))?overValueInput:"0");
        }catch (Exception e){

        }
        int timesLeftInt = 0;
        try {

            timesLeftInt = Integer.parseInt((timesLeft!=null&&!timesLeft.equals(""))?timesLeft:"0");
        }catch (Exception e){
            timesLeftInt = 0;
        }

        Date dateStart = null;
        Date dateEnd = null;

        if(dateRange!=null&&!dateRange.equals("")) {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH);
            try {
                dateStart = format.parse(dateRange.split(" - ")[0]);
                dateEnd = format.parse(dateRange.split(" - ")[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        //Validation

        if(id!=null&&!MongoService.hasDiscountCodeByCode(code)){
            flash("discountCode","Code not found");
        }

        if(id==null&&code!=null&&MongoService.hasDiscountCodeByCode(code)) {
            flash("discountCode","Code already in use");
            return redirect(routes.DiscountCodeController.discountCode(null));
        }

        if(validation.equals(Utils.DiscountValidation.collections)&&collectionList.size()==0){
            flash("discountCode","Need Some Collections for this validation");
            return redirect(routes.DiscountCodeController.discountCode(null));
        }


        //build inventory object
        DiscountCode discount = null;
        if(id!=null) {
            discount = MongoService.findDiscountCodeById(id);
        }

        discount = (discount!=null)?discount:new DiscountCode();
        discount.setCode((discount.getCode() == null) ? code : discount.getCode());

        discount.setTypeForPay(typeDiscount);
        discount.setValueOf(value);
        discount.setActive(activeBool);

        discount.setNoDateLimits(noDateLimiteBool);
        discount.setStartDate(dateStart);
        discount.setEndDate(dateEnd);

        discount.setNoTimesLimits(noLimitsBool);
        discount.setTimesLeft(timesLeftInt);

        discount.setOrdersValidation(validation);
        discount.setWhereApply(paymentAdjust);

        discount.setOnLocalStore(onLocalStoreBool);
        discount.setOnLineVisible(onLineVisibleBool);


        switch (validation) {

            case collections: {
                discount.setCollectionsSlug(collectionList);
                discount.setOverValueOf(0);
                discount.setProductSlugs(new HashSet<>());
                break;
            }
            case overValue: {
                discount.setCollectionsSlug(new HashSet<>());
                discount.setOverValueOf(overValueInputDouble);
                discount.setProductSlugs(new HashSet<>());
                break;

            }
            case specificProduct:{
                discount.setCollectionsSlug(new HashSet<>());
                discount.setOverValueOf(0);
                discount.setProductSlugs(productsnList);
                break;
            }
            default:{
                discount.setCollectionsSlug(new HashSet<>());
                discount.setOverValueOf(0);
                discount.setProductSlugs(new HashSet<>());
                break;

            }

        }

        //save Discount Code
        MongoService.saveDiscountCode(discount);





        return redirect(routes.DiscountCodeController.listDiscount());
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteDiscountCode(String id){
        DiscountCode discountCode = MongoService.findDiscountCodeById(id);
        if(discountCode!=null){
            MongoService.deleteDiscountCode(discountCode);
            flash("listDiscountCode","Removed with success");
        }
        return redirect(routes.DiscountCodeController.listDiscount());
    }


     @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result discountCode(String id){
        DiscountCode discountCode = MongoService.findDiscountCodeById(id);
        discountCode= (discountCode!=null?discountCode:new DiscountCode());
        List<Collection> collections =  MongoService.getAllCollections();
        List<Product> products = MongoService.getAllProducts();

        return ok(newDiscountCode.render(discountCode,collections,products));
    }

    



}
