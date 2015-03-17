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

public class MongoDbPlugin extends Plugin {
    public MongoDbPlugin(Application application) {
        this.application = application;
    }

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

    public static MongoDbPlugin getMongoDbPlugin() {
        play.Application app = Play.application();

        MongoDbPlugin mongoDbPlugin = app.plugin(MongoDbPlugin.class);

        if (mongoDbPlugin == null) {
            throw new RuntimeException("Unable to obtain MongoDb Plugin!");
        }

        return mongoDbPlugin;
    }

    public Jongo getJongoDBInstance(String databaseName) {
        return new Jongo(mongoClient.getDB(databaseName));
    }

    public DB getMongoDBInstance(String databaseName) {
        return mongoClient.getDB(databaseName);
    }

    public List<String> getDatabaseNames() {
        List<String> databaseNames = mongoClient.getDatabaseNames();
        return databaseNames;
    }

    private Application application;
    private MongoClient mongoClient;
}
