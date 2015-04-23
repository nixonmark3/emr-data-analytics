package controllers;

import play.libs.Json;
import play.mvc.Result;

import java.util.List;
import java.io.ByteArrayInputStream;

import services.BlockResultsService;

public class BlockResults extends ControllerBase {

    public static Result getAvailableResults(String blockName) {

        return ok(Json.toJson(BlockResultsService.getAvailableResults(blockName)));
    }

    public static Result getStatistics(String blockName) {

        return ok(Json.toJson(BlockResultsService.getStatistics(blockName)));
    }

    public static Result getPlot(String blockName) {

        ByteArrayInputStream image = new ByteArrayInputStream(BlockResultsService.getPlot(blockName));

        if (image != null) {

            return ok(image).as("image/jpeg");
        }

        return notFound();
    }

    public static Result getOutputResults(String blockName) {

        List<String> outputResults = BlockResultsService.getOutputResults(blockName);

        return ok(Json.toJson(outputResults));
    }
}
