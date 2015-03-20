package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import models.diagram.BasicDiagram;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

import org.jongo.*;

import models.diagram.Diagram;

/**
 * Diagrams Controller.
 */
public class Diagrams extends ControllerBase {
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
        try {
            // Deserialize json sent by client to Diagram model object.
            ObjectMapper objectMapper = new ObjectMapper();
            Diagram diagram = objectMapper.convertValue(request().body().asJson(), Diagram.class);

            MongoCollection diagrams = getMongoCollection(DIAGRAMS_COLLECTION);

            if (diagrams != null) {
                // Need to ensure that each diagram has a unique name.
                diagrams.ensureIndex(DIAGRAM_INDEX, UNIQUE_IS_TRUE);
                // Update the Diagram document in MongoDB. If it does not exist create a new Diagram document.
                diagrams.update(String.format(QUERY_BY_UNIQUE_ID, diagram.getName())).upsert().with(diagram);
                // After we successfully update the diagram bump the version.
                diagrams.update(String.format(QUERY_BY_UNIQUE_ID, diagram.getName())).with(VERSION_INCREMENT);
            }
            else {
                return internalServerError(String.format(COLLECTION_NOT_FOUND, DIAGRAMS_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError(String.format("Failed to save diagram."));
        }

        return ok("Diagram saved successfully.");
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
    public static Result removeDiagram(String diagramName) {
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
    private static final String DIAGRAM_PROJECTION = "{_id: 0, name: 1, description: 1}";
    private static final String DIAGRAMS_COLLECTION = "diagrams";
    private static final String QUERY_BY_UNIQUE_ID = "{name: '%s'}";
    private static final String SORT_BY_NAME = "{name: 1}";
    private static final String COLLECTION_NOT_FOUND = "'%s' collection could not be found!";
}
