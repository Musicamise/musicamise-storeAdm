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

public class CostumerController extends Controller {


    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    public static String generatePassword() {

        return new BigInteger(130, new SecureRandom()).toString(32);
    }
    
    @Security.Authenticated(Secured.class)
    public static Result listCostumers() {
        List<User> users = MongoService.getAllUsers();
        return ok(listCostumer.render(users));
    }
        @AddCSRFToken
    @Security.Authenticated(Secured.class)
    public static Result costumer(String id){
        User user = MongoService.findUserById(id);
        user= (user!=null?user:new User());
        List<String> userTags = MongoService.getAllTags().stream().map(Tag::getSlug).collect(Collectors.toList());
        return ok(newCostumer.render(user,userTags));
    }
    

        @RequireCSRFCheck
    @Security.Authenticated(Secured.class)
    public static Result saveCostumer(String id){

        //Http.MultipartFormData dataFiles = request().body().asMultipartFormData();
        Map<String, String[]> dataFiles = request().body().asFormUrlEncoded();
        String fullname = (dataFiles.get("fullname") != null && dataFiles.get("fullname").length > 0) ? dataFiles.get("fullname")[0] : null;
        String firstname = (dataFiles.get("firstname") != null && dataFiles.get("firstname").length > 0) ? dataFiles.get("firstname")[0] : null;
        String lastname = (dataFiles.get("lastname") != null && dataFiles.get("lastname").length > 0) ? dataFiles.get("lastname")[0] : null;
        String email = (dataFiles.get("email") != null && dataFiles.get("email").length > 0) ? dataFiles.get("email")[0] : null;
        String mktAccept = (dataFiles.get("mktAccept") != null && dataFiles.get("mktAccept").length > 0) ? dataFiles.get("mktAccept")[0] : null;
        String notes = (dataFiles.get("notes") != null && dataFiles.get("notes").length > 0) ? dataFiles.get("notes")[0] : null;
        String[] tags = (dataFiles.get("tags") != null && dataFiles.get("tags").length > 0) ? dataFiles.get("tags") : null;
        List<String> tagsList =  (tags!=null)?Arrays.asList(tags):new ArrayList<>();
        boolean mktAcceptBool = (mktAccept!=null)?true:false;

        //Validation

        if(id!=null&&!MongoService.hasUserById(id)) {
            flash("costumer","User not in base");
            return redirect(routes.CostumerController.costumer(id!=null?id:null));
        }

        if((fullname==null||fullname.equals(""))&&(firstname==null||firstname.equals(""))){
            flash("costumer","Insert First name or Full name");
            return redirect(routes.CostumerController.costumer(id!=null?id:null));
        }
        if(email==null||email.equals("")||!isValidEmailAddress(email)){
            flash("costumer","Isert Email correctly");
            return redirect(routes.CostumerController.costumer(id!=null?id:null));
        }
        if (id==null&&MongoService.hasUserByEmail(email)){
            flash("costumer","Email already in use");
            return redirect(routes.CostumerController.costumer(id!=null?id:null));
        }
        // if (id!=null&&!(id.equals(email))&&MongoService.hasUserByEmail(email)){
        //     flash("costumer","Changing the Email with other already in use");
        //     return redirect(routes.Application.costumer(id));
        // }

        //build object User
        User  user = null;

        if(id==null){
            user = new User(firstname,email,generatePassword());
        }else{
            user = MongoService.findUserById(id);
        }

        user.setFullName((fullname != null && !fullname.equals("")) ? fullname : user.getFullName());
        user.setFirstName((firstname != null && !firstname.equals("")) ? firstname : user.getFirstName());
        user.setLastName((lastname != null && !lastname.equals("")) ? lastname : user.getLastName());
        // user.setEmail((email != null && !email.equals("")) ? email : user.getEmail());
        user.setMarketingEmail(mktAcceptBool);
        user.setNotes((notes != null && !notes.equals("")) ? notes : user.getNotes());
        user.setTags(tagsList);

        int count = dataFiles.get("address") != null ? dataFiles.get("address").length : 0;
        List<Address> addressesList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Address addressObj = new Address();
            String name = (dataFiles.get("name") != null && dataFiles.get("name").length > i) ? dataFiles.get("name")[i] : null;
            String address = (dataFiles.get("address") != null && dataFiles.get("address").length > i) ? dataFiles.get("address")[i] : null;
            String number = (dataFiles.get("number") != null && dataFiles.get("number").length > i) ? dataFiles.get("number")[i] : null;
            String city = (dataFiles.get("city") != null && dataFiles.get("city").length > i) ? dataFiles.get("city")[i] : null;
            String state = (dataFiles.get("state") != null && dataFiles.get("state").length > i) ? dataFiles.get("state")[i] : null;
            String country = (dataFiles.get("country") != null && dataFiles.get("country").length > i) ? dataFiles.get("country")[i] : null;
            String zipcode = (dataFiles.get("zipcode") != null && dataFiles.get("zipcode").length > i) ? dataFiles.get("zipcode")[i] : null;
            addressObj.setName(name);
            addressObj.setAddress(address);
            addressObj.setCity(city);
            addressObj.setState(state);
            addressObj.setNumber(number);
            addressObj.setCountry(country);
            addressObj.setCep(zipcode);
            addressesList.add(addressObj);
        }

        user.setAddress(addressesList);

        //save user or update
        MongoService.saveUser(user);

        return redirect(routes.CostumerController.listCostumers());
    }

        @Security.Authenticated(Secured.class)
    public static Result deleteCostumer(String id){
        User user = MongoService.findUserByEmail(id);
        if(user!=null){
            MongoService.deleteUser(user);
            flash("listCostumers","Removed with success");
        }
        return redirect(routes.CostumerController.listCostumers());

    }

    @Security.Authenticated(Secured.class)
    public static Result unsubscribe(String userId){
        String message = "";
        
        Try{

            User user = MongoService.findUserById(id);
            if(user!=null){
                user.setMarketingEmail(false);
                MongoService.deleteUser(user);
                message ="Email removido com sucesso!";
            }else{
                mesage = "Usuário não encontrado!";
            }

        }catch(Exception e){
            message = "Algum erro aconteceu, tente novamente atualizando a página!"
        }
        return ok(cancelamentoEmail.render(message));

    }
    




}