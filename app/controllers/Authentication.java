package controllers;

import models.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.mindrot.jbcrypt.BCrypt;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.MongoService;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.UUID;
import play.Logger;
import play.filters.csrf.*;


public class Authentication extends Controller {

    // public static User getUser(){
    //     User user = null;
    //     String uuid = session("UUID");
    //     String email = session("username");
    //     if(uuid!=null) {
    //         user = (User) Cache.get(uuid + "_user");
    //     }

    //     if(user ==null) {
    //         user = MongoService.findUserByEmail(email);
    //         updateCache(user);
    //     }
    //     return user;
    // }

    // public static boolean isLogged(){
        
    //     String email = session("username");
    //     //Logger.debug("Email: "+(email==null));
    //     return email!=null;

    // }

    // public static Result redefinirSenha(String tokenSenha ,String email) {

    //     if(confirmTokenAndUser(email,tokenSenha)){
    //         return ok(views.html.novaSenha.render(email,tokenSenha));
    //     }

    //     return notFound(views.html.static_notFound.render());


    // }

    // private static boolean confirmTokenAndUser(String email,String tokenSenha){
    //     if(email!=null &&!email.equals("")) {
    //         User user = MongoService.findUserByEmail(email);
    //         if (tokenSenha != null && !tokenSenha.equals("")) {
    //             if (user != null) {
    //                 return user.getToken().equals(tokenSenha);
    //             }
    //         }
    //     }
    //     return false;
    // }

    // public static Result saveNewPass() {
    //     Map<String, String[]> data = request().body().asFormUrlEncoded();
    //     String pass = (data.get("password")!=null&&data.get("password").length>0)?data.get("password")[0]:"";
    //     String confirm = (data.get("passwordConfirm")!=null&&data.get("passwordConfirm").length>0)?data.get("passwordConfirm")[0]:"";

    //     String message = "";
    //     if(!pass.equals("")&&!confirm.equals("")) {
    //         if (pass.equals(confirm)) {
    //             String[] token = data.get("token");
    //             String[] email = data.get("email");
    //             if (confirmTokenAndUser(email[0], token[0])){
    //                 User user = MongoService.findUserByEmail(email[0]);
    //                 user.setEncryptPass(pass);
    //                 user.setActive(true);
    //                 user.generateToken();
    //                 MongoService.saveUser(user);

    //                 message = "Sucesso!";

    //             }
    //         }else{
    //             message = "Preencha corretamente ambos campos!";
    //         }
    //     }else{
    //         message = "Preencha corretamente!";
    //     }

    //     return redirect(routes.Application.signin());
    // }

    // public static Result esqueciSenha() {

    //    String message = flash("esqueciSenha");


    //     return ok(views.html.esqueciSenha.render(message));
    // }

    // public static Result esqueciSenhaPost(){
    //     Map<String, String[]> data = request().body().asFormUrlEncoded();
    //     String email = (data.get("formLoginEmailUser")!=null&&data.get("formLoginEmailUser").length>0)?data.get("formLoginEmailUser")[0]:"";
    //     User user = MongoService.findUserByEmail(email);
    //     if(!email.equals("")&&user!=null){

    //         user.generateToken();
    //         String fullUrl = routes.Authentication.redefinirSenha(user.getToken(),user.getEmail()).absoluteURL(request());

    //         try {
    //             SendEmail.sendEmailForgetPass(user, fullUrl);
    //             MongoService.saveUser(user);
    //         } catch (MessagingException e) {
    //             e.printStackTrace();
    //             return badRequest(views.html.static_error.render());
    //         }
    //     }else{
    //         flash("esqueciSenha","Coloque um email cadastrado.");

    //         return redirect(routes.Authentication.esqueciSenha());
    //     }
    //     flash("esqueciSenha","Email enviado para "+email);

    //     return redirect(routes.Authentication.esqueciSenha());

    // }
    
    // @RequireCSRFCheck
    // public static Result sendEmailContact(){
    //     Map<String, String[]> data = request().body().asFormUrlEncoded();
    //     String name = (data.get("name")!=null&&data.get("name").length>0)?data.get("name")[0]:"";
    //     String email = (data.get("email")!=null&&data.get("email").length>0)?data.get("email")[0]:"";
    //     String subject = (data.get("subject")!=null&&data.get("subject").length>0)?data.get("subject")[0]:"";
    //     String message = (data.get("message")!=null&&data.get("message").length>0)?data.get("message")[0]:"";

    //     if(!email.equals("")&&!name.equals("")&&!subject.equals("")&&!message.equals("")){

    //         try {
    //             SendEmail.sendEmailContact(name,message,email,subject);

    //         } catch (MessagingException e) {
    //             e.printStackTrace();
    //             return badRequest();

    //         }
    //     }else{
    //         return badRequest();
    //     }

    //     return ok();

    // }

    public static Result authenticate() {

        Map<String, String[]> login_data = request().body().asFormUrlEncoded();
        String[] login_email = login_data.get("formLoginEmailUser");
        String[] login_password = login_data.get("formLoginPassword");

        if (login_email != null && login_email.length == 1 && login_password != null && login_password.length == 1) {
            User user = MongoService.findUserByEmail(login_email[0]);
            if (user!=null&&user.isManager() && BCrypt.checkpw(login_password[0], user.getPassword())) {
                session().clear();
                String uuid = UUID.randomUUID().toString();
                session("UUID", uuid);
                session("username", user.getEmail());
                Cache.set(uuid + "_user", user,60*15);
                return redirect(routes.Application.dashboard());
            }
        }
        flash("signin-admin", "Login ou Senha não conferem. Tente Novamente.");

        return redirect(routes.Application.login());
    }

    // public static Result authenticateUser() {
    //     Map<String, String[]> login_data = request().body().asFormUrlEncoded();
    //     String[] login_email = login_data.get("email");
    //     String[] login_password = login_data.get("password");
    //     String[] referer = login_data.get("referer");


    //     if (login_email != null && login_email.length == 1 && login_password != null && login_password.length == 1) {
    //         User user = MongoService.findUserByEmail(login_email[0]);

    //         if (user!=null && BCrypt.checkpw(login_password[0], user.getPassWord())) {
    //             String uuid = session("UUID");
    //             if(uuid==null) {
    //                 session().clear();
    //                 uuid = UUID.randomUUID().toString();
    //                 session("UUID", uuid);
    //                 session("username", user.getEmail());
    //                 Cache.set(uuid + "_user", user,60*15);
    //             }else{
    //                 session("username", user.getEmail());
    //                 Cache.set(uuid + "_user", user,60*15);
    //             }
    //             flash("signin", "");
    //             if(referer[0].contains("checkout") ){
    //                 return redirect(routes.Application.wsGetCompraIdAndRedirect());
    //             }else {
    //                 return redirect(routes.Application.index());
    //             }
    //         }
    //     }
    //     flash("signin","Login ou Senha não conferem. Tente Novamente.");

    //     return redirect(routes.Application.signin());
    // }

    // public static void authenticateUserOnSignUp(User user) {

    //         //User user = MongoService.findUserByEmail(email);

    //         String uuid = session("UUID");
    //         if(uuid==null) {
    //             session().clear();
    //             uuid = UUID.randomUUID().toString();
    //             session("UUID", uuid);
    //             session("username", user.getEmail());
    //             Cache.set(uuid + "_user", user,60*15);
    //         }else{
    //             session("username", user.getEmail());
    //             Cache.set(uuid + "_user", user,60*15);
    //         }
    //        // return redirect(routes.Application.index());

    // }

    // public static void updateCache(User user){
    //     String uuid = session("UUID");

    //     Cache.set(uuid + "_user", user,60*15);
    // }



}
