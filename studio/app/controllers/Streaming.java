package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.messages.StreamingRequest;
import emr.analytics.models.messages.StreamingSummaryRequest;
import emr.analytics.models.messages.StreamingTerminationRequest;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import services.AnalyticsService;

/**
 * Interface used to manage Streaming Sources
 */
public class Streaming extends Controller {

    /**
     * Start streaming task
     * @return: task id
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result start() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StreamingRequest request = objectMapper.convertValue(request().body().asJson(), StreamingRequest.class);

            AnalyticsService.getInstance().send(request);

            return ok(request.getId().toString());
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to initialize streaming source.");
        }
    }

    /**
     * Terminate streaming task
     * @param topic: topic of the task to terminate
     * @return: task Id
     */
    public static Result stop(String topic){

        StreamingTerminationRequest request = new StreamingTerminationRequest(topic);
        AnalyticsService.getInstance().send(request);

        return ok(request.getId().toString());
    }

    /**
     * Request a summary of all streaming tasks
     * @return: task id
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result summary(){

        ObjectMapper objectMapper = new ObjectMapper();
        StreamingSummaryRequest request = objectMapper.convertValue(request().body().asJson(), StreamingSummaryRequest.class);

        AnalyticsService.getInstance().send(request);
        return ok(request.getId().toString());
    }
}
