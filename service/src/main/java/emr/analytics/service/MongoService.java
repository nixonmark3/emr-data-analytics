package emr.analytics.service;

import emr.analytics.database.MongoConnection;

import java.util.Properties;

public class MongoService {

    private static MongoService _instance;
    private MongoConnection mongoConnection;

    public MongoService(){
        Properties properties = TaskProperties.getInstance().getProperties();
        String host = properties.getProperty("mongo.mongodb.host");
        String defaultDatabaseName = properties.getProperty("mongo.database.name");

        mongoConnection = new MongoConnection(host, defaultDatabaseName);
    }

    public static MongoService getInstance() {

        if(_instance == null) {
            synchronized (MongoService.class) {

                if (_instance == null)
                    _instance = new MongoService();
            }
        }
        return _instance;
    }

    public MongoConnection getConnection(){
        return this.mongoConnection;
    }
}
