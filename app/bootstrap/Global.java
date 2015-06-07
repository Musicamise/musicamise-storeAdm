package bootstrap; /**
 * Created by Alvaro on 18/03/2015.
 */
import models.*;
import models.Content;
import play.*;
import play.mvc.*;
import play.mvc.Http.*;
import play.libs.F.*;
import services.MongoService;
import services.SendEmail;

import javax.mail.MessagingException;
import java.util.List;
import play.api.mvc.EssentialFilter;
import play.filters.csrf.CSRFFilter;

import static play.mvc.Results.*;

public class Global extends GlobalSettings {

     @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{CSRFFilter.class};
    }

    public void onStart(Application app) {
         DS.init();
         MongoService.createInitialColletions();
        //MongoHandler.getInstance(null);
        //models.Content test = new models.Content();
        //test.setTitle("Teste title alvaro@silvino.me");
        //test.setContent("alvaro");
        //test.setType(Utils.Content.ABOUT);
        //createUser();

        //ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig1.class);
        //MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");

        Logger.info("Application has started");
    }
    public static void createUser(){
        User user = new User("Admin", "pedro.almeida@revistafirstdown.com.br", "admin");
        //user.setManager(true);
        MongoService.saveUser(user);
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }

    public Promise<Result> onError(RequestHeader request, Throwable t) {
        return Promise.<Result>pure(internalServerError(
                //views.html.static_error.render(t)
                views.html.static_error.render()

        ));
    }

    public Promise<Result> onHandlerNotFound(RequestHeader request) {
        return Promise.<Result>pure(notFound(
                // views.html.static_notFound.render(request.uri())
                views.html.static_notFound.render()

        ));
    }
    public Promise<Result> onBadRequest(RequestHeader request, String error) {
        return Promise.<Result>pure(badRequest("Don't try to hack the URI!"));
    }

    // For CORS
    private class ActionWrapper extends Action.Simple {
        public ActionWrapper(Action<?> action) {
            this.delegate = action;
        }

        @Override
        public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
            Promise<Result> result = this.delegate.call(ctx);
            Http.Response response = ctx.response();
            response.setHeader("Access-Control-Allow-Origin", "*");
            return result;
        }
    }

    @Override
    public Action<?> onRequest(Http.Request request,
                               java.lang.reflect.Method actionMethod) {
        return new ActionWrapper(super.onRequest(request, actionMethod));
    }



}