package controllers;

import play.*;
import play.mvc.*;
import views.html.*;

/**
 * Main Application Controller.
 */
public class Application extends Controller {

    public static Result index(String any) {
        return ok(index.render("Data Analytics Studio"));
    }

    public static Result javascriptRoutes() {

        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.routes.javascript.Analytics.socket())
        );
    }
}
