package controllers;

import play.mvc.*;
import play.Logger.ALogger;

import views.html.*;

/**
 * Main Application Controller.
 */
public class Application extends Controller {
    public static Result index(String any) {
//        ALogger log = play.Logger.of("application");
//        log.info("Data Analytics Studio application started.");
        return ok(index.render("Data Analytics Studio"));
    }
}
