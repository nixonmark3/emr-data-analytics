package controllers;

import actors.SessionActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.DiagramContainer;
import emr.analytics.models.messages.JobKillRequest;
import emr.analytics.models.messages.JobRequest;
import play.mvc.*;
import services.AnalyticsService;
import services.DiagramsService;

import java.util.UUID;

public class Analytics extends Controller {

    public static WebSocket<JsonNode> socket(String id) { return WebSocket.withActor(out -> SessionActor.props(out, UUID.fromString(id))); }

    /**
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result evaluate() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            // save the diagram
            DiagramsService diagramsService = DiagramsService.getInstance();
            UUID id = diagramsService.save(diagramContainer);

            Diagram offline = diagramContainer.getOffline();
            if (offline.getId() == null)
                offline.setId(id);

            JobRequest jobRequest = diagramsService.getJobRequest(offline);

            AnalyticsService.getInstance().send(jobRequest);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to evaluate diagram.");
        }

        return ok("Diagram evaluation initiated.");
    }

    /**
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result deploy() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            // save the diagram
            DiagramsService diagramsService =DiagramsService.getInstance();
            diagramsService.save(diagramContainer);

            Diagram online = diagramContainer.getOnline();
            JobRequest jobRequest = diagramsService.getJobRequest(online);

            AnalyticsService.getInstance().send(jobRequest);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to deploy diagram.");
        }

        return ok("Diagram deployment initiated.");
    }

    /**
     *
     * @return
     */
    public static Result kill(String id, String mode) {

        JobKillRequest jobKillRequest = new JobKillRequest(UUID.fromString(id), Mode.valueOf(mode));
        AnalyticsService.getInstance().send(jobKillRequest);

        return ok("Job killing process initiated.");
    }
}
