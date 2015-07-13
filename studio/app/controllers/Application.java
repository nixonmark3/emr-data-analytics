package controllers;

import play.mvc.*;
import views.html.*;

/**
 * Main Application Controller.
 */
public class Application extends Controller {

    public static Result index(String any) {
        return ok(index.render("Data Analytics Studio"));
    }

}
