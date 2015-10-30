package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.Definition;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import plugins.MongoDBPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DefinitionsService {

    private static final String DEFINITION_COLLECTION = "definitions";
    private static final String BY_CATEGORY_AND_NAME = "{category: 1, name: 1}";

    public static HashMap<String, Definition> getDefinitionsMap() {

        HashMap<String, Definition> definitionMap = new HashMap<String, Definition>();

        try {
            getDefinitionsDbCollection().find().as(Definition.class).forEach(definition -> {
                definitionMap.put(definition.getName(), definition);
            });

            for(Definition definition : getEmbeddedDefinitions())
                definitionMap.put(definition.getName(), definition);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return definitionMap;
    }

    public static List<Definition> getDefinitions(){

        List<Definition> definitions = new ArrayList<Definition>();

        try {
            getDefinitionsDbCollection().find().as(Definition.class).forEach(definition -> {
                definitions.add(definition);
            });

            definitions.addAll(getEmbeddedDefinitions());

            Collections.sort(definitions);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return definitions;
    }

    private static MongoCollection getDefinitionsDbCollection() {

        MongoCollection definitions = null;
        MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

        if (mongoPlugin != null) {

            Jongo db = mongoPlugin.getJongoDBInstance(mongoPlugin.getStudioDatabaseName());
            if (db != null) {

                definitions = db.getCollection(DEFINITION_COLLECTION);
            }
        }

        return definitions;
    }

    private static List<Definition> getEmbeddedDefinitions(){

        InputStream stream = null;
        try {
            stream = DefinitionsService.class.getClassLoader().getResourceAsStream("embeddedDefinitions.json");
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(stream, new TypeReference<List<Definition>>(){});
        }
        catch(IOException ex){

            throw new RuntimeException(ex);
        }
        finally{

            try {
                if (stream != null)
                    stream.close();
            }
            catch(IOException ex) {

                throw new RuntimeException(ex);
            }
        }
    }
}
