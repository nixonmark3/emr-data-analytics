package controllers;

import models.DataSet;

import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import play.libs.Json;
import play.mvc.Result;

import plugins.MongoDBPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Projects Controller.
 */
public class Projects extends ControllerBase {
    /**
     * Returns a collection with the name of each data project.
     * @return list of available projects
     */
    public static Result getProjects() {
        MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

        List<String> projectNames = new ArrayList<String>();

        for (String databaseName : mongoPlugin.getDatabaseNames()) {
            if (databaseName.contains(PROJECT_PREFIX)) {
                projectNames.add(databaseName.replace(PROJECT_PREFIX, ""));
            }
        }

        return ok(Json.toJson(projectNames));
    }

    /**
     * Returns the Data Sets from the requested Project database.
     * @param projectName project database name
     * @return list of data set names
     */
    public static Result getDataSet(String projectName) {
        MongoCursor<DataSet> dataSets = null;

        projectName = PROJECT_PREFIX.concat(projectName);

        try {
            MongoCollection dataSetsCollection = getMongoCollection(projectName, DATA_SET_COLLECTION);

            if (dataSetsCollection != null) {
                dataSets = dataSetsCollection.find().projection(DATA_SET_PROJECTION).sort(SORT_BY_NAME).as(DataSet.class);
            }
            else {
                return internalServerError(String.format(COLLECTION_NOT_FOUND, DATA_SET_COLLECTION));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get data sets.");
        }

        return ok(Json.toJson(dataSets));
    }

    /**
     * Private constants.
     */
    private static final String PROJECT_PREFIX = "bds-";
    private static final String DATA_SET_PROJECTION = "{_id: 0, name: 1}";
    private static final String DATA_SET_COLLECTION = "datasets";
    private static final String SORT_BY_NAME = "{name: 1}";
    private static final String COLLECTION_NOT_FOUND = "'%s' collection could not be found!";

}
