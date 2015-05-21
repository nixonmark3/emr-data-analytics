package emr.analytics.wrapper;

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

    public static void createDefinitions() {

        try {

            MongoClient connection = new MongoClient();

            DB db = connection.getDB("emr-data-analytics-studio");

            Jongo jongo = new Jongo(db);
            MongoCollection definitions = jongo.getCollection("definitions");

            definitions.drop();

            definitions.ensureIndex("{name: 1}", "{unique:true}");

            DefinitionGenerator generator = new DefinitionGenerator(definitions);

            generator.generate();

            connection.close();
        }
        catch (java.net.UnknownHostException exception) {

            exception.printStackTrace();
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }
    }
}
