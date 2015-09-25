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

public class Dashboard extends Controller {

   
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
    public static Result getUsuariosGrowTotal(){
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

        List<DBObject> results = aggResult.getMappedResults();
        int total = 0;
        for (int i =0; i<aggResult.getMappedResults().size();i++ ) {
            total += (int)results.get(i).get("quantity");
            results.get(i).put("total", total);
        }


        return ok(results.toString());
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
            if(entry.getQuantity()>0){

                if(!results.has(newstring)){
                    results.put(newstring,Json.newObject());
                }
                if(!results.with(newstring).has(entry.getInventory().getName())){
                    results.with(newstring).put(entry.getInventory().getName(),0);
                }
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
            if(entry.getQuantity()<0){

                if(!results.has(newstring)){
                    results.put(newstring,Json.newObject());
                }
                if(!results.with(newstring).has(entry.getInventory().getName())){
                    results.with(newstring).put(entry.getInventory().getName(),0);
                }
                int newValue = results.with(newstring).findValue(entry.getInventory().getName()).intValue()+entry.getQuantity();
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
        aggResult = MongoService.getDashboardProductsFaturamento(startDate, endDate);
        return ok(aggResult.getRawResults().toString());
    }
    @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result getFaturamento(){
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
        List<DBObject> results = aggResult.getMappedResults();
        double fat = 0;
        for (int i =0; i<aggResult.getMappedResults().size();i++ ) {
            fat += (double)results.get(i).get("total");
            results.get(i).put("total", fat);
        }


        return ok(results.toString());
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



}
