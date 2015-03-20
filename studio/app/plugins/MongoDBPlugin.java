package plugins;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import org.jongo.Jongo;

import play.Application;
import play.Configuration;
import play.Play;
import play.Plugin;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin that abstracts connection to MongoDB.
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
     * Override of plugin start method.
     * Open connection to MongoDB.
     */
    @Override
    public void onStart() {
        Configuration config = Configuration.root().getConfig(MONGODB_CONFIG);

        String host = config.getString(MONGODB_HOST_NAME);

        try {
            mongoClient = new MongoClient(host);
        }
        catch (UnknownHostException exception) {
            exception.printStackTrace();
            throw new RuntimeException("Unable to create MongoDB instance!", exception);
        }
    }

    /**
     * Override the plugin stop method.
     * Close connection to MongoDB.
     */
    @Override
    public void onStop() {
        if (mongoClient != null) {
            mongoClient.close();
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
            throw new RuntimeException("Unable to get MongoDB Plugin!");
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
            exception.printStackTrace();
        }

        return db;
    }

    /**
     * Returns a list of the available databases in MongoDB.
     * @return list of database names
     */
    public List<String> getDatabaseNames() {
        List<String> databaseNames = new ArrayList<String>();

        try {
            databaseNames = mongoClient.getDatabaseNames();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return databaseNames;
    }

    /**
     * Returns the name of the studio application database.
     * @return studio database name
     */
    public String getStudioDatabaseName() {
        return Configuration.root().getConfig(MONGODB_CONFIG).getString(MONGODB_DB_NAME);
    }

    /**
     * Private members.
     */
    private Application application;
    private MongoClient mongoClient;

    /**
     * Private constants.
     */
    private static final String MONGODB_CONFIG = "mongo";
    private static final String MONGODB_HOST_NAME = "mongodb.host";
    private static final String MONGODB_DB_NAME = "database.name";
}
