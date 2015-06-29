package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import emr.analytics.models.diagram.Diagram;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import services.BlockResultsService;

public class BlockResults extends ControllerBase {

    public static Result getAvailableResults(String blockName) {

        return ok(Json.toJson(BlockResultsService.getAvailableResults(blockName)));
    }

    public static Result getStatistics(String blockName) {

        return ok(Json.toJson(BlockResultsService.getStatistics(blockName)));
    }

    public static Result getFeatures(String blockName) {

        return ok(Json.toJson(BlockResultsService.getFeatures(blockName)));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getChartData(String blockName) {

        ObjectMapper objectMapper = new ObjectMapper();
        BasicDBObject selectedFeatures = objectMapper.convertValue(request().body().asJson(), BasicDBObject.class);

        List<String> features = (ArrayList<String>)selectedFeatures.get("features");

        return ok(Json.toJson(BlockResultsService.getChartData(blockName, features)));
    }

    public static Result getPlot(String blockName) {

        byte[] image = BlockResultsService.getPlot(blockName);

        if (image != null) {
            return ok(Base64.getEncoder().encodeToString(image));
        }

        return notFound();
    }

    public static Result getOutputResults(String blockName) {

        return ok(Json.toJson(BlockResultsService.getOutputResults(blockName)));
    }
}
