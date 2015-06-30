package controllers;

import actors.ClientActorManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.WriteResult;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.*;

import models.project.GroupRequest;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

import org.jongo.*;
import services.*;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Diagrams extends ControllerBase {

    private static HashMap<String, Definition> _definitions = DefinitionsService.getDefinitionsMap();

    // initialize the evaluation service Akka components
    private static EvaluationService _evaluationService = new EvaluationService(_definitions);

    private static Map<UUID, UUID> _mapJobIdToClientId = new HashMap<UUID, UUID>();

    public static UUID getClientIdForJob(UUID jobId) {
        return _mapJobIdToClientId.get(jobId);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result debug() {
        ObjectMapper objectMapper = new ObjectMapper();
        Diagram diagram = objectMapper.convertValue(request().body().asJson(), Diagram.class);

        String source = _evaluationService.sendCompileRequest(diagram);
        return ok(source);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result deploy() {

        ObjectMapper objectMapper = new ObjectMapper();
        DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

        DiagramsService diagramsService = new DiagramsService();
        diagramsService.save(diagramContainer);

        Diagram onlineDiagram = diagramContainer.getOnline();

        UUID jobId = _evaluationService.sendRequest(onlineDiagram);

        if (jobId == null) {

            return internalServerError("Error requesting evaluation.");
        }

        return ok(jobId.toString());
    }

    public static Result kill(String jobId) {

        UUID id = UUID.fromString(jobId);

        boolean result = _evaluationService.sendKillRequest(id);

        return ok("Diagram job has been successfully killed.");
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result evaluate(String clientId) {

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            DiagramsService diagramsService = new DiagramsService();
            diagramsService.save(diagramContainer);

            Diagram offlineDiagram = diagramContainer.getOffline();

            UUID jobId = _evaluationService.sendRequest(offlineDiagram);

            if (jobId != null) {

                _mapJobIdToClientId.put(jobId, UUID.fromString(clientId));

                DiagramStates.createDiagramState(offlineDiagram, jobId);
            } else {

                return internalServerError("Error requesting evaluation.");
            }
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError(String.format("Failed to evaluate diagram."));
        }

        return ok("Diagram evaluation initiated.");
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result compile() {

        Diagram online = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            DiagramsService diagramsService = new DiagramsService();
            diagramsService.save(diagramContainer);

            Diagram offlineDiagram = diagramContainer.getOffline();

            DiagramCompiler diagramCompiler = new DiagramCompiler(_definitions);
            online = diagramCompiler.compile(offlineDiagram);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError(String.format("Failed to compile diagram."));
        }

        return ok(Json.toJson(online));
    }

    public static Result getDiagram(String diagramName) {

        Diagram diagram = null;

        try {
            MongoCollection diagrams = getMongoCollection(DIAGRAMS_COLLECTION);

            if (diagrams != null) {
                diagram = diagrams.findOne(String.format(QUERY_BY_UNIQUE_ID, diagramName)).as(Diagram.class);
            }
            else {
                return internalServerError(String.format(COLLECTION_NOT_FOUND, DIAGRAMS_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError(String.format("Failed to get diagram '%s'.", diagramName));
        }

        if (diagram == null) {
            return notFound("The diagram '%s' could not be found.", diagramName);
        }

        return ok(Json.toJson(diagram));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result group() {

        GroupRequest request;
        Diagram diagram = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.convertValue(request().body().asJson(), GroupRequest.class);

            DiagramsService service = new DiagramsService();
            diagram = service.group(request);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError(String.format("Failed to group blocks."));
        }

        return ok(Json.toJson(diagram));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result saveDiagram() {

        DiagramContainer diagramContainer = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            DiagramsService service = new DiagramsService();
            service.save(diagramContainer);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError(String.format("Failed to save diagram."));
        }

        return ok("Diagram saved successfully.");
    }

    public static Result getDiagrams() {
        MongoCursor<BasicDiagram> diagrams = null;

        try {
            MongoCollection diagramsCollection = getMongoCollection(DIAGRAMS_COLLECTION);

            if (diagramsCollection != null) {
                diagrams = diagramsCollection.find().projection(DIAGRAM_PROJECTION).sort(SORT_BY_NAME).as(BasicDiagram.class);
            }
            else {
                return internalServerError(String.format(COLLECTION_NOT_FOUND, DIAGRAMS_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get diagrams.");
        }

        return ok(Json.toJson(diagrams));
    }

    public static Result deleteDiagram(String diagramName) {
        try {
            MongoCollection diagrams = getMongoCollection(DIAGRAMS_COLLECTION);

            if (diagrams != null) {
                diagrams.remove(String.format(QUERY_BY_UNIQUE_ID, diagramName));
            }
            else {
                return internalServerError(String.format(COLLECTION_NOT_FOUND, DIAGRAMS_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError(String.format("Failed to remove diagram."));
        }

        ObjectNode addDiagramMsg = Json.newObject();
        addDiagramMsg.put("messageType", "DeleteDiagram");
        addDiagramMsg.put("name", diagramName);
        ClientActorManager.getInstance().updateClients(addDiagramMsg);

        // todo delete all other document related to this diagram

        return ok("Diagram removed successfully.");
    }

    public static Result getBlankDiagram() {
        return ok(Json.toJson(Diagram.Create()));
    }

    private static final String DIAGRAM_PROJECTION = "{_id: 0, name: 1, description: 1, owner: 1}";
    private static final String DIAGRAMS_COLLECTION = "diagrams";
    private static final String QUERY_BY_UNIQUE_ID = "{name: '%s'}";
    private static final String SORT_BY_NAME = "{name: 1}";
    private static final String COLLECTION_NOT_FOUND = "'%s' collection could not be found!";
}