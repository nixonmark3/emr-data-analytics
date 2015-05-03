import com.mongodb.MongoClient;
import emr.analytics.models.definition.Argument;
import emr.analytics.models.interfaces.DynamicSource;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Bricks implements DynamicSource {

        private static final String PROJECT_PREFIX = "bricks-";

        public List<String> Execute(List< Argument > arguments) {
        List<String> projectNames = new ArrayList<String>();

        try {
            MongoClient mongoClient = new MongoClient();
            for (String databaseName : mongoClient.getDatabaseNames()) {
                if (databaseName.contains(PROJECT_PREFIX)) {
                    projectNames.add(databaseName.replace(PROJECT_PREFIX, ""));
                }
            }
            mongoClient.close();
        }
        catch (UnknownHostException exception) {
            exception.printStackTrace();
        }

        System.out.println("Bricks called!");
        return projectNames;
    }

}
