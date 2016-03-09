package emr.analytics.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.Definition;
import org.jongo.MongoCollection;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DefinitionsRepository {

    private static final String collectionName = "definitions";

    private MongoCollection collection;

    public DefinitionsRepository(MongoConnection connection){
        this.collection = connection.getCollection(collectionName);
    }

    public List<Definition> toList(){

        try {
            List<Definition> definitions = new ArrayList<Definition>();
            this.collection.find().as(Definition.class).forEach(definitions::add);

            for(Definition definition : this.getEmbeddedDefinitions())
                definitions.add(definition);

            return definitions;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
    }

    public Map<String, Definition> toMap() {


        try {
            Map<String, Definition> definitions = new HashMap<String, Definition>();
            this.collection.find().as(Definition.class).forEach(definition -> {
                definitions.put(definition.getName(), definition);
            });

            for(Definition definition : this.getEmbeddedDefinitions())
                definitions.put(definition.getName(), definition);

            return definitions;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
    }

    private List<Definition> getEmbeddedDefinitions(){

        InputStream stream = null;
        try {
            stream = this.getClass().getClassLoader().getResourceAsStream("embeddedDefinitions.json");
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
