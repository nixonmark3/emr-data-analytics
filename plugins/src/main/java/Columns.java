import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.BasicDBList;

import emr.analytics.models.definition.Argument;
import emr.analytics.models.interfaces.DynamicSource;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Columns implements DynamicSource {

    public List<String> Execute(List<Argument> arguments) {

        String blockName = arguments.get(0).getValue();

        List<String> stringList = new ArrayList<String>();

        MongoClient mongoClient = null;

        try {

            mongoClient = new MongoClient();
            DBCollection results = mongoClient.getDB("emr-data-analytics-studio").getCollection("results");

            BasicDBObject query = new BasicDBObject("name", blockName);

            DBCursor cursor = results.find(query);

            try {

                while(cursor.hasNext()) {
                    BasicDBList columns = (BasicDBList)cursor.next().get("Columns");

                    for (Object column : columns) {
                        stringList.add((String)column);
                    }
                }
            }
            finally {
                if (cursor != null) {
                    cursor.close();
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

        return stringList;
    }
}
