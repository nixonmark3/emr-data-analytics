package controllers;

import emr.analytics.database.ProjectsRepository;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import plugins.MongoPlugin;

/**
 * Projects Controller.
 */
public class Projects extends Controller {

    private static ProjectsRepository repository = new ProjectsRepository(MongoPlugin.getPlugin().getConnection());

    /**
     * Returns a collection with the name of each data project.
     * @return list of available projects
     */
    public static Result getProjects() {
        return ok(Json.toJson(repository.all()));
    }

    /**
     * Returns the Data Sets from the requested Project database.
     * @param projectName project database name
     * @return list of data set names
     */
    public static Result getDataSet(String projectName) {

        try {
            String results = repository.getDataSet(projectName);
            return ok(results);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return internalServerError("Failed to get data sets.");
        }
    }

}
