package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import emr.analytics.database.ResultsRepository;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import plugins.MongoPlugin;

public class BlockResults extends Controller {

    public static ResultsRepository repository = new ResultsRepository(MongoPlugin.getPlugin().getConnection());

    public static Result getAvailableResults(String blockName) {

        String results;

        try {
            results = repository.getAvailableResults(blockName);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get available results.");
        }

        return ok(results);
    }

    public static Result getStatistics(String blockName) {

        String results;

        try {
            results = repository.getStatistics(blockName);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get statistics.");
        }

        return ok(results);
    }

    public static Result getFeatures(String blockName) {

        String results;

        try {
            results = repository.getStatistics(blockName);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get features.");
        }

        return ok(results);
    }

    public static Result getFeatureGridData(String blockName) {

        String results;

        try {
            results = repository.getFeatureGridData(blockName);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get chart data.");
        }

        return ok(results);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getChartData(String blockName) {

        String results;

        try {

            BasicDBObject selectedFeatures = new ObjectMapper().convertValue(request().body().asJson(), BasicDBObject.class);
            List<String> features = (ArrayList<String>) selectedFeatures.get("features");

            results = repository.getChartData(blockName, features);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get chart data.");
        }

        return ok(results);
    }

    public static Result getPlot(String blockName) {

        String encodedImage;

        try {

            byte[] image = repository.getPlot(blockName);

            if (image != null)
                encodedImage = Base64.getEncoder().encodeToString(image);
            else
                return notFound();
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to get plot.");
        }

        return ok(encodedImage);
    }

    public static Result getOutputResults(String blockName) {

        String results;

        try {

            results = repository.getOutputResults(blockName);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get output results.");
        }

        return ok(results);
    }
}
