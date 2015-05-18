package controllers;

import actors.ClientActorManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.*;

import emr.analytics.service.jobs.JobMode;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

import org.jongo.*;
import services.BlockResultsService;
import services.DefinitionsService;
import services.DiagramsService;
import services.EvaluationService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Diagrams Controller.
 */
public class Diagrams extends ControllerBase {

    private static HashMap<String, Definition> _definitions = DefinitionsService.getDefinitionsMap();

    // initialize the evaluation service Akka components
    private static EvaluationService _evaluationService = new EvaluationService(_definitions);

    private static Map<UUID, UUID> _mapJobIdToClientId = new HashMap<UUID, UUID>();

    public static UUID getClientIdForJob(UUID jobId) {
        return _mapJobIdToClientId.get(jobId);
    }

    /**
     * Evaluates the diagram specified in the request body
     * @return temporarily return success message
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result deploy() {
        ObjectMapper objectMapper = new ObjectMapper();
        Diagram diagram = objectMapper.convertValue(request().body().asJson(), Diagram.class);

        // retrieve models
        HashMap<String, String> models = new HashMap<>();
        BlockResultsService service = new BlockResultsService();
        for(Block block : diagram.getBlocksWithOfflineComplements()){

            String model = BlockResultsService.getModel(block.getOfflineComplement());
            models.put(block.getOfflineComplement(), model);
        }

        UUID jobId = _evaluationService.sendRequest(JobMode.Online, diagram, models);
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
        ObjectMapper objectMapper = new ObjectMapper();
        Diagram diagram = objectMapper.convertValue(request().body().asJson(), Diagram.class);

        saveDiagram(diagram);

        UUID jobId = _evaluationService.sendRequest(JobMode.Offline, diagram);
        if (jobId != null) {
            _mapJobIdToClientId.put(jobId, UUID.fromString(clientId));

            DiagramStates.createDiagramState(diagram, jobId);
        }
        else {
            return internalServerError("Error requesting evaluation.");
        }

        return ok("Diagram evaluation initiated.");
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result compile(){
        ObjectMapper objectMapper = new ObjectMapper();
        Diagram offline = objectMapper.convertValue(request().body().asJson(), Diagram.class);

        DiagramsService service = new DiagramsService(_definitions);
        Diagram online = service.compile(offline);

        return ok(Json.toJson(online));
    }

    /**
     * Returns the specified diagram
     * @return Json representing requested diagram or failure
     */
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

    /**
     * Save the Diagram sent in the request body.
     * @return success or failure result
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result saveDiagram() {

        Diagram diagram = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            diagram = objectMapper.convertValue(request().body().asJson(), Diagram.class);

            saveDiagram(diagram);

        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError(String.format("Failed to save diagram."));
        }

        return ok("Diagram saved successfully.");
    }

    private static void saveDiagram(Diagram diagram) {

        try {

            MongoCollection diagrams = getMongoCollection(DIAGRAMS_COLLECTION);

            if (diagrams != null) {

                // Need to ensure that each diagram has a unique name.
                diagrams.ensureIndex(DIAGRAM_INDEX, UNIQUE_IS_TRUE);
                // Update the Diagram document in MongoDB. If it does not exist create a new Diagram document.
                WriteResult update = diagrams.update(String.format(QUERY_BY_UNIQUE_ID, diagram.getName())).upsert().with(diagram);

                // todo determine if the save succeeded?

                // After we successfully update the diagram bump the version.
                diagrams.update(String.format(QUERY_BY_UNIQUE_ID, diagram.getName())).with(VERSION_INCREMENT);

                ObjectNode addDiagramMsg = Json.newObject();

                if (update.isUpdateOfExisting()) {

                    addDiagramMsg.put("messageType", "UpdateDiagram");
                }
                else {

                    addDiagramMsg.put("messageType", "AddDiagram");
                }

                addDiagramMsg.put("name", diagram.getName());
                addDiagramMsg.put("description", diagram.getDescription());
                addDiagramMsg.put("owner", diagram.getOwner());

                ClientActorManager.getInstance().updateClients(addDiagramMsg);
            }
        }
        catch (Exception expception) {

            expception.printStackTrace();
        }
    }

    /**
     * Returns a collection with the name and description of each available diagram.
     * @return list of available diagrams
     */
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

    /**
     * Remove the specified diagram from the database.
     * @param diagramName name of diagram
     * @return success of failure result
     */
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

    /**
     * Returns a blank diagram.
     * @return Json representing a blank diagram
     */
    public static Result getBlankDiagram() {
        return ok(Json.toJson(BasicDiagram.CreateBasicDiagram()));
    }

    /**
     * Private constants.
     */
    private static final String DIAGRAM_INDEX = "{name: 1}";
    private static final String UNIQUE_IS_TRUE = "{unique:true}";
    private static final String VERSION_INCREMENT = "{$inc: {version: 1}}";
    private static final String DIAGRAM_PROJECTION = "{_id: 0, name: 1, description: 1, owner: 1}";
    private static final String DIAGRAMS_COLLECTION = "diagrams";
    private static final String QUERY_BY_UNIQUE_ID = "{name: '%s'}";
    private static final String SORT_BY_NAME = "{name: 1}";
    private static final String COLLECTION_NOT_FOUND = "'%s' collection could not be found!";
}