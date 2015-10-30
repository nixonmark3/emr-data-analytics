package controllers;

import emr.analytics.models.definition.Definition;

import org.jongo.*;

import play.libs.Json;
import play.mvc.Result;
import services.DefinitionsService;

import java.util.ArrayList;
import java.util.List;

public class Definitions  extends ControllerBase {

    public static Result getDefinitions() {

        List<Definition> definitions = null;
        try {

            definitions = DefinitionsService.getDefinitions();
        }
        catch (Exception ex) {

            return internalServerError(ex.getMessage());
        }

        return ok(Json.toJson(definitions));
    }
}
