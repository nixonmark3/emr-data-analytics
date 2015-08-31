package emr.analytics.wrapper;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws java.net.UnknownHostException {

        System.out.println("Wrapper started.");

        System.out.println("Creating definitions.");

        boolean status = createDefinitions();

        if (status) {
            System.out.println("Definitions created successfully.");
        }
        else {
            System.out.println("Failed to create definitions.");
        }

        System.out.println("Wrapper stopped.");
    }

    public static boolean createDefinitions() {

        boolean status = true;

        MongoClient connection = null;

        try {
            Properties properties = loadProperties("app");
            String host = properties.getProperty("mongo.host");
            int port = Integer.parseInt(properties.getProperty("mongo.port"));

            connection = new MongoClient(host, port);

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

    private static Properties loadProperties(String name){

        String fileName = String.format("%s.properties", name);

        try (InputStream stream = Main.class.getClassLoader().getResourceAsStream(fileName)){
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
