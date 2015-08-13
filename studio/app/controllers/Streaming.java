package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.messages.StreamingSourceKillRequest;
import emr.analytics.models.messages.StreamingSourceRequest;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import services.AnalyticsService;

/**
 * Interface used to manage Streaming Sources
 */
public class Streaming extends Controller {

    /**
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result start() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StreamingSourceRequest request = objectMapper.convertValue(request().body().asJson(), StreamingSourceRequest.class);

            AnalyticsService.getInstance().send(request);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to initialize streaming source.");
        }

        return ok();
    }

    /**
     *
     * @param topic
     * @return
     */
    public static Result stop(String topic){

        StreamingSourceKillRequest request = new StreamingSourceKillRequest(topic);
        AnalyticsService.getInstance().send(request);

        return ok();
    }
}
