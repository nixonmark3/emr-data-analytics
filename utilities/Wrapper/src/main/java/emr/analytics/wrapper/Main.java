package emr.analytics.wrapper;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class Main {

    public static void main(String[] args) throws java.net.UnknownHostException {

        System.out.println("Creating Definitions...");

        boolean status = createDefinitions();

        if (status) {
            System.out.println("Complete!");
        }
        else {
            System.out.println("Failed!");
        }
    }

    public static boolean createDefinitions() {

        boolean status = true;

        MongoClient connection = null;

        try {

            connection = new MongoClient();

            DB db = connection.getDB("emr-data-analytics-studio");

            Jongo jongo = new Jongo(db);
            MongoCollection definitions = jongo.getCollection("definitions");

            definitions.drop();

            definitions.ensureIndex("{name: 1}", "{unique:true}");

            DefinitionGenerator generator = new DefinitionGenerator(definitions);

            generator.generate();

        }
        catch (java.net.UnknownHostException exception) {

            status = false;
            exception.printStackTrace();
        }
        catch (Exception exception) {

            status = false;
            exception.printStackTrace();
        }
        finally {

            connection.close();
        }

        return status;
    }
}
