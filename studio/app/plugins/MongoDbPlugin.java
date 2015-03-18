package plugins;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import org.jongo.Jongo;

import play.Application;
import play.Configuration;
import play.Play;
import play.Plugin;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Plugin that abstracts connection to Mongo Database.
 */
public class MongoDBPlugin extends Plugin {

    /**
     * Constructor for MongoDB plugin.
     * @param application the application instance
     */
    public MongoDBPlugin(Application application) {
        this.application = application;
    }

    /**
     * Override of plugin startup method.
     */
    @Override
    public void onStart() {
        Configuration config = Configuration.root().getConfig("mongo");

        String host = config.getString("mongodb.host");

        try {
            mongoClient = new MongoClient(host);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to create Mongo instance.", e);
        }
    }

    /**
     * Returns an instance of the MongoDB plugin.
     * @return MongoDBPlugin
     */
    public static MongoDBPlugin getMongoDbPlugin() {
        play.Application app = Play.application();

        MongoDBPlugin mongoDBPlugin = app.plugin(MongoDBPlugin.class);

        if (mongoDBPlugin == null) {
            throw new RuntimeException("Unable to obtain MongoDb Plugin!");
        }

        return mongoDBPlugin;
    }

    /**
     * Returns an instance of the requested database through Jongo.
     * @param databaseName The name of the database to use
     * @return Jongo instance
     */
    public Jongo getJongoDBInstance(String databaseName) {
        return new Jongo(this.getMongoDBInstance(databaseName));
    }

    /**
     * Returns an instance of the requested database.
     * @param databaseName The name of the database to use
     * @return MongoDB instance
     */
    public DB getMongoDBInstance(String databaseName) {
        DB db = null;

        try {
            db = mongoClient.getDB(databaseName);
        }
        catch (Exception exception) {
            System.out.println(exception.getMessage()); // TODO
        }

        return db;
    }

    /**
     * Returns a list of the available databases in MongoDB.
     * @return list of database names
     */
    public List<String> getDatabaseNames() {
        List<String> databaseNames = null;

        try {
            databaseNames = mongoClient.getDatabaseNames();
        }
        catch (Exception exception) {
            System.out.println(exception.getMessage()); // TODO
        }

        return databaseNames;
    }

    /**
     * Return the name of the studio database
     * @return studio database name
     */
    public String getStudioDatabaseName() {
        return Configuration.root().getConfig("mongo").getString("database.name");
    }

    /**
     * Private members
     */
    private Application application;
    private MongoClient mongoClient;
}
