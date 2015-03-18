package controllers;

import models.definition.*;

import org.jongo.*;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import plugins.MongoDBPlugin;

import java.util.*;

/**
 * Definitions Controller.
 */
public class Definitions  extends Controller {
    /**
     * Returns all the Definitions and Categories.
     * @return Json representing requested definition and categories
     */
    public static Result getDefinitions() {
        MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

        Jongo db = mongoPlugin.getJongoDBInstance(mongoPlugin.getStudioDatabaseName());

        MongoCursor<Category> definitionCategories = null;

        try {
            MongoCollection definitions = db.getCollection("definitions");

            definitionCategories = definitions.find("{}").as(Category.class);
        }
        catch (Exception exception) {
            return internalServerError(exception.getMessage());
        }

        List<Category> categoryList = new ArrayList<Category>();

        if (definitionCategories != null){
            for (Category category : definitionCategories) {
                categoryList.add(category);
            }
        }

        return ok(Json.toJson(categoryList));
    }
}
