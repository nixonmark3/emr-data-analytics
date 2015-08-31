package services;

import emr.analytics.models.definition.Definition;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import plugins.MongoDBPlugin;

import java.util.HashMap;

public class DefinitionsService {

    public static HashMap<String, Definition> getDefinitionsMap() {

        HashMap<String, Definition> definitionMap = new HashMap<String, Definition>();

        try {
            getDefinitionsDbCollection().find().as(Definition.class).forEach(d -> {
                definitionMap.put(d.getName(), d);
            });
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return definitionMap;
    }

    private static MongoCollection getDefinitionsDbCollection() {

        MongoCollection definitions = null;

        MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

        if (mongoPlugin != null) {

            Jongo db = mongoPlugin.getJongoDBInstance(mongoPlugin.getStudioDatabaseName());

            if (db != null) {

                definitions = db.getCollection("definitions");
            }
        }

        return definitions;
    }
}
