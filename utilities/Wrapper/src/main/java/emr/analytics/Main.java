package emr.analytics;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class Main {

    public static void main(String[] args) throws java.net.UnknownHostException {
        System.out.println("Creating Definitions...");
        createDefinitions();
        System.out.println("Complete!");
    }

    public static void createDefinitions() throws java.net.UnknownHostException {
        MongoClient connection = new MongoClient();

        DB db = connection.getDB("emr-data-analytics-studio");

        Jongo jongo = new Jongo(db);
        MongoCollection definitions = jongo.getCollection("definitions");

        // Remove all the definitions as we will recreate all of them
        definitions.drop();

        // Need to ensure that each category has a unique name
        definitions.ensureIndex("{name: 1}", "{unique:true}");

        DefinitionGenerator generator = new DefinitionGenerator(definitions);

        generator.generate();

        connection.close();
    }

}
