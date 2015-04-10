package controllers;

import emr.analytics.models.definition.Definition;

import org.jongo.*;

import play.libs.Json;
import play.mvc.Result;

/**
 * Definitions Controller.
 */
public class Definitions  extends ControllerBase {
    private static final String DEFINITION_COLLECTION = "definitions";

    /**
     * Returns all the Definitions and Categories.
     * @return Json representing requested definition and categories
     */
    public static Result getDefinitions() {
        MongoCursor<Definition> definitions = null;

        try {
            MongoCollection definitionCollection = getMongoCollection(DEFINITION_COLLECTION);

            if (definitionCollection != null) {
                definitions = definitionCollection.find().as(Definition.class);
            }
            else {
                return internalServerError(String.format("'%s' collection could not be found!", DEFINITION_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get definitions.");
        }

        return ok(Json.toJson(definitions));
    }
}
