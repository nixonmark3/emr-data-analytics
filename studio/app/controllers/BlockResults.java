package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
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

        List<String> availableResults;

        try {

            availableResults = new BlockResultsService().getAvailableResults(blockName);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to get available results.");
        }

        return ok(Json.toJson(availableResults));
    }

    public static Result getStatistics(String blockName) {

        BasicDBList statistics;

        try {

            statistics = new BlockResultsService().getStatistics(blockName);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to get statistics.");
        }

        return ok(Json.toJson(statistics));
    }

    public static Result getFeatures(String blockName) {

        BasicDBList features;

        try {

            features = new BlockResultsService().getStatistics(blockName);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to get features.");
        }

        return ok(Json.toJson(features));
    }

    public static Result getFeatureGridData(String blockName) {

        BasicDBList chartData;

        try {

            chartData = new BlockResultsService().getFeatureGridData(blockName);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to get chart data.");
        }

        return ok(Json.toJson(chartData));
    }

        @BodyParser.Of(BodyParser.Json.class)
    public static Result getChartData(String blockName) {

        BasicDBList chartData;

        try {

            BasicDBObject selectedFeatures = new ObjectMapper().convertValue(request().body().asJson(), BasicDBObject.class);

            List<String> features = (ArrayList<String>) selectedFeatures.get("features");

            chartData = new BlockResultsService().getChartData(blockName, features);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to get chart data.");
        }

        return ok(Json.toJson(chartData));
    }

    public static Result getPlot(String blockName) {

        String encodedImage;

        try {

            byte[] image = new BlockResultsService().getPlot(blockName);

            if (image != null) {

                encodedImage = Base64.getEncoder().encodeToString(image);
            }
            else {

                return notFound();
            }
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to get plot.");
        }

        return ok(encodedImage);
    }

    public static Result getOutputResults(String blockName) {

        List<BasicDBObject> outputResults;

        try {

            outputResults = new BlockResultsService().getOutputResults(blockName);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to get output results.");
        }

        return ok(Json.toJson(outputResults));
    }
}
