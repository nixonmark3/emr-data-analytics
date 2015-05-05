import com.mongodb.MongoClient;

import emr.analytics.models.definition.Argument;
import emr.analytics.models.interfaces.DynamicSource;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Projects implements DynamicSource {
    private static final String PROJECT_PREFIX = "das-";

    public List<String> Execute(List<Argument> arguments) {
        List<String> projectNames = new ArrayList<String>();

        MongoClient mongoClient = null;

        try {
            mongoClient = new MongoClient();
            for (String databaseName : mongoClient.getDatabaseNames()) {
                if (databaseName.contains(PROJECT_PREFIX)) {
                    projectNames.add(databaseName.replace(PROJECT_PREFIX, ""));
                }
            }
        }
        catch (UnknownHostException exception) {
            exception.printStackTrace();
        }
        finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

        return projectNames;
    }
}
