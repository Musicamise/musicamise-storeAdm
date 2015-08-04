/**
 * 
 */
package controllers;

import models.User;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import services.MongoService;

/**
 * @author Alvaro
 *
 */
public class Secured extends Security.Authenticator {

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }

    @Override
    public String getUsername(Context context) {
        String a = super.getUsername(context);
        User user = MongoService.findUserByEmail(a);
        //verificar o timestamp
        return (user!=null&&user.isManager())?a:null;
    }
}