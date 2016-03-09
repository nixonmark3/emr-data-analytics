package plugins;

import emr.analytics.database.MongoConnection;

import play.Application;
import play.Configuration;
import play.Play;
import play.Plugin;

public class MongoPlugin extends Plugin {

    private static final String MONGODB_CONFIG = "mongo";
    private static final String MONGODB_HOST_NAME = "mongodb.host";
    private static final String MONGODB_DB_NAME = "database.name";

    private Application application;
    private MongoConnection mongoConnection;

    public MongoPlugin(Application application) {
        this.application = application;
    }

    @Override
    public void onStart() {
        String host = getMongoHostName();
        String defaultDatabaseName = getStudioDatabaseName();
        mongoConnection = new MongoConnection(host, defaultDatabaseName);
    }

    @Override
    public void onStop() {
        if (mongoConnection != null) {
            mongoConnection.close();
        }
    }

    public static MongoPlugin getPlugin() {
        play.Application app = Play.application();

        MongoPlugin plugin = app.plugin(MongoPlugin.class);

        if (plugin == null)
            throw new RuntimeException("Unable to get MongoDB Plugin!");

        return plugin;
    }

    public MongoConnection getConnection(){
        return this.mongoConnection;
    }

    public String getMongoHostName() {
        return Configuration.root().getConfig(MONGODB_CONFIG).getString(MONGODB_HOST_NAME);
    }

    public String getStudioDatabaseName() {
        return Configuration.root().getConfig(MONGODB_CONFIG).getString(MONGODB_DB_NAME);
    }
}
