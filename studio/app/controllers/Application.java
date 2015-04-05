package controllers;

import actors.ClientActor;

import play.mvc.*;

import com.fasterxml.jackson.databind.JsonNode;

import views.html.*;

/**
 * Main Application Controller.
 */
public class Application extends Controller {
    public static Result index(String any) {
        return ok(index.render("Data Analytics Studio"));
    }

    public static WebSocket<JsonNode> clientSocket() {
        return WebSocket.withActor(ClientActor::props);
    }

}
