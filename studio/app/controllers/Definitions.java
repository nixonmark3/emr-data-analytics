package controllers;

import emr.analytics.database.DefinitionsRepository;
import emr.analytics.models.definition.Definition;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import plugins.MongoPlugin;

import java.util.Collections;
import java.util.List;

public class Definitions  extends Controller {

    private static DefinitionsRepository repository = new DefinitionsRepository(MongoPlugin.getPlugin().getConnection());

    public static Result getDefinitions() {

        List<Definition> definitions = null;
        try {
            definitions = repository.toList();
            Collections.sort(definitions);
        }
        catch (Exception ex) {

            return internalServerError(ex.getMessage());
        }

        return ok(Json.toJson(definitions));
    }
}
