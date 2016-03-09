package controllers;

import actors.SessionActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.database.DiagramsRepository;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.DiagramContainer;
import emr.analytics.models.messages.*;
import models.CollectRequest;
import models.DescribeRequest;
import models.LoadRequest;
import play.libs.Json;
import play.mvc.*;
import plugins.MongoPlugin;
import services.AnalyticsService;

import java.util.UUID;

public class Analytics extends Controller {

    public static WebSocket<JsonNode> socket(String id) { return WebSocket.withActor(out -> SessionActor.props(out, UUID.fromString(id))); }

    /**
     * Initiates a data collection request
     * @return the unique id assigned to the task request
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result collect(){

        // this collect method is alway performed in the offline mode
        Mode mode = Mode.OFFLINE;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CollectRequest collectRequest = objectMapper.convertValue(request().body().asJson(), CollectRequest.class);

            TaskRequest request = new TaskRequest(collectRequest.getSessionId(),
                    collectRequest.getDiagramId(),
                    mode,
                    collectRequest.getTargetEnvironment(),
                    collectRequest.getDiagramName(),
                    collectRequest.getCode());

            // send request to analytics service
            AnalyticsService.getInstance().send(request);

            // return the task request's guid
            return ok(request.getId().toString());
        }
        catch (Exception ex){

            return internalServerError(ex.getMessage());
        }
    }

    /**
     * Submit a request to deploy the specified diagram
     * @return id of the task request
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result deploy() {

        try {
            // deserialize the diagram container
            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            // save the diagram
            MongoPlugin plugin = MongoPlugin.getPlugin();
            DiagramsRepository repository = new DiagramsRepository(plugin.getConnection());
            repository.save(diagramContainer);

            // create a diagram task request and send it to the analytics service
            Diagram online = diagramContainer.getOnline();
            DiagramTaskRequest request = new DiagramTaskRequest(online);
            AnalyticsService.getInstance().send(request);

            return ok(request.getId().toString());
        }
        catch (Exception ex) {

            ex.printStackTrace();

            String errorMessage = ex.getMessage();
            if (errorMessage == null)
                errorMessage = ex.toString();

            return internalServerError(errorMessage);
        }
    }

    /**
     * Submit a request to describe the data associated with the specified block connections
     * @return: if of the task request
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result describe(){

        // this describe method is alway performed in the offline mode
        Mode mode = Mode.OFFLINE;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DescribeRequest describeRequest = objectMapper.convertValue(request().body().asJson(), DescribeRequest.class);

            TaskRequest request = new TaskRequest(describeRequest.getSessionId(),
                    describeRequest.getDiagramId(),
                    mode,
                    describeRequest.getTargetEnvironment(),
                    describeRequest.getDiagramName(),
                    describeRequest.getCode());

            // send request to analytics service
            AnalyticsService.getInstance().send(request);

            // return the task request's guid
            return ok(request.getId().toString());
        }
        catch (Exception ex){
            return internalServerError(ex.getMessage());
        }
    }

    /**
     * Submit a request to evaluate the specified diagram
     * @return: id of the TaskRequest
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result evaluate() {

        try {
            // deserialize the diagram container
            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            // save the diagram
            MongoPlugin plugin = MongoPlugin.getPlugin();
            DiagramsRepository repository = new DiagramsRepository(plugin.getConnection());
            UUID id = repository.save(diagramContainer);

            // reference the offline diagram and if necessary add the id
            Diagram offline = diagramContainer.getOffline();
            if (offline.getId() == null)
                offline.setId(id);

            // create a diagram task request and send it to the analytics service
            DiagramTaskRequest request = new DiagramTaskRequest(offline);
            AnalyticsService.getInstance().send(request);

            return ok(request.getId().toString());
        }
        catch (Exception ex) {

            ex.printStackTrace();

            String errorMessage = ex.getMessage();
            if (errorMessage == null)
                errorMessage = ex.toString();

            return internalServerError(errorMessage);
        }
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
     * Initiates a load request
     * @return the unique id assigned to the task request
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result load() {

        // this load method is alway performed in the offline mode
        Mode mode = Mode.OFFLINE;

        try {

            // parse load request
            ObjectMapper objectMapper = new ObjectMapper();
            LoadRequest load = objectMapper.convertValue(request().body().asJson(), LoadRequest.class);

            // generate code and create task request
            BlockTaskRequest request = new BlockTaskRequest(load.getSessionId(),
                    load.getDiagramId(),
                    mode,
                    load.getTargetEnvironment(),
                    load.getDiagramName(),
                    load.getBlock());

            // send request to analytics service
            AnalyticsService.getInstance().send(request);

            // return the task request's guid
            return ok(request.getId().toString());
        }
        catch (Exception ex){

            ex.printStackTrace();

            String errorMessage = ex.getMessage();
            if (errorMessage == null)
                errorMessage = ex.toString();

            return internalServerError(errorMessage);
        }
    }

    /**
     * Get list of current tasks associated with the specified diagram id
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
     * Terminate the tasks session associated with the specified diagram id and mode
     * @return: 200 ok
     */
    public static Result terminate(String id, String mode) {

        TerminationRequest request = new TerminationRequest(UUID.fromString(id), Mode.valueOf(mode));
        AnalyticsService.getInstance().send(request);

        return ok();
    }


}
