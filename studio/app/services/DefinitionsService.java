package services;

import com.mongodb.MongoClient;
import emr.analytics.models.definition.Definition;
import org.jongo.Jongo;

import java.net.UnknownHostException;
import java.util.HashMap;

public class DefinitionsService {

    public static HashMap<String, Definition> getDefinitionsMap() {

        HashMap<String, Definition> definitionMap = new HashMap<String, Definition>();
        MongoClient mongoClient = null;

        try {
            mongoClient = new MongoClient();

            Jongo db = new Jongo(mongoClient.getDB("emr-data-analytics-studio"));

            db.getCollection("definitions").find().as(Definition.class).forEach(d -> {
                definitionMap.put(d.getName(), d);
            });
        }
        catch (UnknownHostException excpetion) {
            excpetion.printStackTrace();
        }
        finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

        return definitionMap;
    }
}
