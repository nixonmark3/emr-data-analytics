package controllers;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import plugins.MongoDbPlugin;

import org.jongo.*;

import models.diagram.Diagram;

/**
 * Diagrams controller.
 */
public class Diagrams extends Controller {
    /**
     * Returns the requested diagram
     * @return Json representing requested diagram
     */
    public static Result getDiagram(String diagramName) {
        MongoDbPlugin mongoPlugin = MongoDbPlugin.getMongoDbPlugin();

        Jongo db = mongoPlugin.getJongoDBInstance(mongoPlugin.getStudioDatabaseName());

        MongoCollection diagrams = db.getCollection("diagrams");

        String query = String.format("{name: '%s'}", diagramName);

        Diagram diagram = null;

        try {
            diagram = diagrams.findOne(query).as(Diagram.class);
        }
        catch (Exception exception) {
            return internalServerError(exception.getMessage());
        }

        if (diagram == null) {
            return notFound();
        }

        return ok(Json.toJson(diagram));
    }
}
