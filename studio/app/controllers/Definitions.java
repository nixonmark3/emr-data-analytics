package controllers;

import emr.analytics.models.definition.Definition;

import org.jongo.*;

import play.libs.Json;
import play.mvc.Result;

public class Definitions  extends ControllerBase {

    private static final String DEFINITION_COLLECTION = "definitions";
    private static final String BY_CATEGORY_AND_NAME = "{category: 1, name: 1}";
    private static final String COLLECTION_NOT_FOUND = "'%s' collection could not be found!";
    private static final String GET_DEFINITIONS_FAILED = "Failed to get definitions.";

    public static Result getDefinitions() {

        MongoCursor<Definition> definitions = null;

        try {
            MongoCollection definitionCollection = getMongoCollection(DEFINITION_COLLECTION);

            if (definitionCollection != null) {

                definitions = definitionCollection.find().sort(BY_CATEGORY_AND_NAME).as(Definition.class);
            }
            else {

                return internalServerError(String.format(COLLECTION_NOT_FOUND, DEFINITION_COLLECTION));
            }
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError(GET_DEFINITIONS_FAILED);
        }

        return ok(Json.toJson(definitions));
    }
}
