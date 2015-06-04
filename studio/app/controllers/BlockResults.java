package controllers;

import play.libs.Json;
import play.mvc.Result;

import java.util.Base64;

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

    public static Result getChartData(String blockName) {

        return ok(Json.toJson(BlockResultsService.getChartData(blockName)));
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
