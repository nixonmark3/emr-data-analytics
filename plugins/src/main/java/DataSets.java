
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import emr.analytics.models.definition.Argument;
import emr.analytics.models.interfaces.DynamicSource;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataSets implements DynamicSource {

    public List<String> Execute(List<Argument> arguments) {

        // todo: perform some validation on the arguments received

        // reference the project name argument
        String projectName = arguments.get(0).getValue();

        return getDataSets(projectName);
    }

    public List<String> getDataSets(String projectName) {

        List<String> sets = new ArrayList<String>();

        MongoClient mongoClient = null;

        try {
            mongoClient = new MongoClient();

            DBCollection dataSetsCollection = mongoClient.getDB("bricks-" + projectName).getCollection("dataset");

            if (dataSetsCollection != null) {

                DBCursor cursor = dataSetsCollection.find();

                try {
                    while (cursor.hasNext()) {
                        sets.add((String)cursor.next().get("name"));
                    }
                }
                finally {
                    if (cursor != null) {
                        cursor.close();
                    }
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

        return sets;
    }
}
