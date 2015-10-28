package controllers;

import actors.SessionActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.DiagramContainer;
import emr.analytics.models.messages.AnalyticsTasks;
import emr.analytics.models.messages.TaskRequest;
import emr.analytics.models.messages.TaskSummaryRequest;
import models.CollectRequest;
import models.LoadRequest;
import play.libs.Json;
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
    public static Result deploy() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            // save the diagram
            DiagramsService diagramsService =DiagramsService.getInstance();
            diagramsService.save(diagramContainer);

            Diagram online = diagramContainer.getOnline();
            TaskRequest taskRequest = diagramsService.getTaskRequest(online);

            AnalyticsService.getInstance().send(taskRequest);
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

            TaskRequest taskRequest = diagramsService.getTaskRequest(offline);

            AnalyticsService.getInstance().send(taskRequest);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError("Failed to evaluate diagram.");
        }

        return ok("Diagram evaluation initiated.");
    }

    /**
     *
     * @param id
     * @return
     */
    public static Result tasks(String id) {

        AnalyticsTasks tasks = null;

        try {
            TaskSummaryRequest request = new TaskSummaryRequest(UUID.fromString(id));
            tasks = (AnalyticsTasks)AnalyticsService.getInstance().sendSync(request);
        }
        catch(Exception ex){

            ex.printStackTrace();
            return internalServerError("Failed to get analytics tasks.");
        }

        return ok(Json.toJson(tasks));
    }

    /**
     * Execute an ad-hoc block of code in the specified diagram's workspace.
     * Creates an offline task from the specified interpreter request.
     * @return confirmation that the requested task has been initialized.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result interpret() {

        // Get task request
        ObjectMapper objectMapper = new ObjectMapper();
        TaskRequest taskRequest = objectMapper.convertValue(request().body().asJson(),
                TaskRequest.class);

        // send request to analytics service
        AnalyticsService.getInstance().send(taskRequest);

        return ok();
    }

    /**
     *
     * @return
     */
/*    public static Result kill(String id, String mode) {

        JobKillRequest jobKillRequest = new JobKillRequest(UUID.fromString(id), Mode.valueOf(mode));
        AnalyticsService.getInstance().send(jobKillRequest);

        return ok("Job killing process initiated.");
    }*/

    /**
     * Initiates a load request
     * @return confirmation of successful load request initialization
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result load() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoadRequest load = objectMapper.convertValue(request().body().asJson(), LoadRequest.class);

            TaskRequest request = new TaskRequest(load.getDiagramId(),
                    Mode.OFFLINE,
                    TargetEnvironments.PYSPARK,
                    load.getDiagramName(),
                    load.getCode());

            // send request to analytics service
            AnalyticsService.getInstance().send(request);

            return ok();
        }
        catch (Exception ex){

            return internalServerError(ex.getMessage());
        }
    }

    /**
     * Initiates a collect request
     * @return confirmation of successful collect request initialization
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result collect(){

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CollectRequest collectRequest = objectMapper.convertValue(request().body().asJson(), CollectRequest.class);

            TaskRequest request = new TaskRequest(collectRequest.getDiagramId(),
                    Mode.OFFLINE,
                    TargetEnvironments.PYSPARK,
                    collectRequest.getDiagramName(),
                    collectRequest.getCode());

            // send request to analytics service
            AnalyticsService.getInstance().send(request);

            return ok();
        }
        catch (Exception ex){

            return internalServerError(ex.getMessage());
        }
    }
}
