package plugins;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import org.jongo.Jongo;

import play.Configuration;
import play.Play;
import play.Plugin;
import play.Application;

import java.net.UnknownHostException;

import java.util.List;

public class JongoPlugin extends Plugin {
    public JongoPlugin(Application application) {
        this.application = application;
    }

    @Override
    public void onStart() {
        Configuration config = Configuration.root().getConfig("jongo");

        String host = config.getString("mongodb.host");

        try {
            mongoClient = new MongoClient(host);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to create Mongo instance.", e);
        }
    }

    public Jongo getJongoInstance(String databaseName) {
        DB db = mongoClient.getDB(databaseName);
        return new Jongo(db);
    }

    public List<String> getDatabaseNames() {
        List<String> databaseNames = mongoClient.getDatabaseNames();
        return databaseNames;
    }

    public static JongoPlugin getJongoPlugin() {
        play.Application app = Play.application();

        JongoPlugin plugin = app.plugin(JongoPlugin.class);

        if (plugin == null) {
            throw new RuntimeException("Unable to obtain Jongo Plugin. "
                    + "Check if plugin has been declared into your project in conf/play.plugins file. "
                    + "If not, please add line '20000:plugins.JongoPlugin'");
        }

        return plugin;
    }

    private Application application;
    private MongoClient mongoClient;
}