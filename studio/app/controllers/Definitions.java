package controllers;

import models.definition.*;

import org.jongo.*;

import play.libs.Json;
import play.mvc.Result;

/**
 * Definitions Controller.
 */
public class Definitions  extends ControllerBase {
    /**
     * Returns all the Definitions and Categories.
     * @return Json representing requested definition and categories
     */
    public static Result getDefinitions() {
        MongoCursor<Category> definitionCategories = null;

        try {
            MongoCollection definitions = getMongoCollection(DEFINITIONS_COLLECTION);

            if (definitions != null) {
                definitionCategories = definitions.find().as(Category.class);
            }
            else {
                return internalServerError(String.format("'%s' collection could not be found!", DEFINITIONS_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get definitions.");
        }

        return ok(Json.toJson(definitionCategories));
    }

    /**
     * Private constants.
     */
    private static final String DEFINITIONS_COLLECTION = "definitions";
}
