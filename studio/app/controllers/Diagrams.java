package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import emr.analytics.database.DiagramsRepository;
import emr.analytics.models.diagram.*;

import models.GroupRequest;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import plugins.MongoPlugin;
import services.*;

import java.util.List;
import java.util.UUID;

public class Diagrams extends Controller {

    private static DiagramsRepository repository = new DiagramsRepository(MongoPlugin.getPlugin().getConnection());

    /**
     * List all diagrams
     * @return projection of all diagrams
     */
    public static Result all() {
        List<DiagramRoot> diagrams = null;

        try {
            diagrams = repository.all();
        }
        catch (Exception ex) {

            String errorMessage = ex.getMessage();
            if (errorMessage == null)
                errorMessage = ex.toString();

            return internalServerError(errorMessage);
        }

        return ok(Json.toJson(diagrams));
    }

    /**
     * Compile the specified diagram and return the resulting source
     * @return: source code
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result compile(){

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Diagram diagram = objectMapper.convertValue(request().body().asJson(), Diagram.class);

            return ok(CompilerService.getInstance().compile(diagram));
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
     * Delete the specified diagram
     * @param name: diagram name
     * @return: ok response
     */
    public static Result delete(String name) {

        try {
            repository.delete(name);
        }
        catch (Exception ex) {

            String errorMessage = ex.getMessage();
            if (errorMessage == null)
                errorMessage = ex.toString();

            return internalServerError(errorMessage);
        }

        // todo delete all other document related to this diagram

        return ok();
    }

    /**
     * Create a new diagram
     * @return diagram
     */
    public static Result empty() {
        return ok(Json.toJson(Diagram.Create()));
    }

    /**
     * Get the specified diagram
     * @param name: diagram name
     * @return: diagram collection { offline, online }
     */
    public static Result get(String name) {

        Diagram diagram = null;

        try {
            diagram = repository.get(name);
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

    /**
     * Group a set of blocks together
     * @return the diagram container the new group
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result group() {

        GroupRequest request;
        Diagram diagram = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.convertValue(request().body().asJson(), GroupRequest.class);

            CompilerService service = new CompilerService();
            diagram = service.group(request);
        }
        catch (Exception ex) {

            ex.printStackTrace();
            String errorMessage = ex.getMessage();
            if (errorMessage == null)
                errorMessage = ex.toString();

            return internalServerError(errorMessage);
        }

        return ok(Json.toJson(diagram));
    }

    /**
     * Save the specified diagram
     * @return diagram id
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result save() {

        UUID diagramId = null;
        DiagramContainer diagramContainer = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);
            diagramId = repository.save(diagramContainer);
        }
        catch (Exception ex) {

            ex.printStackTrace();
            String errorMessage = ex.getMessage();
            if (errorMessage == null)
                errorMessage = ex.toString();

            return internalServerError(errorMessage);
        }

        return ok(diagramId.toString());
    }

    /**
     * Transform the specified offline diagram into its online equivalent
     * @return the online version of the diagram
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result transform() {

        Diagram online = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            DiagramContainer diagramContainer = objectMapper.convertValue(request().body().asJson(), DiagramContainer.class);

            UUID id = repository.save(diagramContainer);

            Diagram offline = diagramContainer.getOffline();
            if (offline.getId() == null)
                offline.setId(id);

            online = CompilerService.getInstance().transform(offline);
        }
        catch (Exception ex) {

            ex.printStackTrace();
            String errorMessage = ex.getMessage();
            if (errorMessage == null)
                errorMessage = ex.toString();

            return internalServerError(errorMessage);
        }

        return ok(Json.toJson(online));
    }
}