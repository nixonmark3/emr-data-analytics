package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import emr.analytics.diagram.interpreter.CompilerException;
import emr.analytics.models.diagram.*;

import models.project.GroupRequest;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

import org.jongo.*;
import services.*;

import java.util.UUID;

public class Diagrams extends ControllerBase {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result compile() {
        ObjectMapper objectMapper = new ObjectMapper();
        Diagram diagram = objectMapper.convertValue(request().body().asJson(), Diagram.class);

        String source;
        try{
            source = DiagramsService.getInstance().compile(diagram);
        }
        catch(CompilerException ex){
            return internalServerError(String.format("Compile Exception: %s", ex.toString()));
        }

        return ok(source);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result transform() {

        Diagram online = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            UUID id = DiagramsService.getInstance().save(diagramContainer);

            Diagram offline = diagramContainer.getOffline();
            if (offline.getId() == null)
                offline.setId(id);

            online = DiagramsService.getInstance().transform(offline);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError(String.format("Failed to compile diagram."));
        }

        return ok(Json.toJson(online));
    }

    public static Result get(String name) {

        Diagram diagram = null;

        try {
            MongoCollection diagrams = getMongoCollection(DIAGRAMS_COLLECTION);

            if (diagrams != null) {
                diagram = diagrams.findOne(String.format(QUERY_BY_UNIQUE_ID, name)).as(Diagram.class);
            }
            else {
                return internalServerError(String.format(COLLECTION_NOT_FOUND, DIAGRAMS_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError(String.format("Failed to get diagram '%s'.", name));
        }

        if (diagram == null) {
            return notFound("The diagram '%s' could not be found.", name);
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
    public static Result save() {

        UUID diagramId = null;
        DiagramContainer diagramContainer = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            DiagramsService service = new DiagramsService();
            diagramId = service.save(diagramContainer);
        }
        catch (Exception exception) {

            exception.printStackTrace();
            return internalServerError(String.format("Failed to save diagram."));
        }

        return ok(diagramId.toString());
    }

    public static Result all() {
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

    public static Result delete(String name) {
        try {
            MongoCollection diagrams = getMongoCollection(DIAGRAMS_COLLECTION);

            if (diagrams != null) {
                diagrams.remove(String.format(QUERY_BY_UNIQUE_ID, name));
            }
            else {
                return internalServerError(String.format(COLLECTION_NOT_FOUND, DIAGRAMS_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError(String.format("Failed to remove diagram."));
        }

        // todo delete all other document related to this diagram

        return ok("Diagram removed successfully.");
    }

    public static Result empty() {
        return ok(Json.toJson(Diagram.Create()));
    }

    private static final String DIAGRAM_PROJECTION = "{_id: 0, name: 1, description: 1, owner: 1}";
    private static final String DIAGRAMS_COLLECTION = "diagrams";
    private static final String QUERY_BY_UNIQUE_ID = "{name: '%s'}";
    private static final String SORT_BY_NAME = "{name: 1}";
    private static final String COLLECTION_NOT_FOUND = "'%s' collection could not be found!";
}