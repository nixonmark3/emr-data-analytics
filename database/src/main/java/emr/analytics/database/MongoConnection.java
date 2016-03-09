package emr.analytics.database;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import java.util.List;

public class MongoConnection {

    private MongoClient client;
    private String defaultDatabaseName;

    public MongoConnection(String host, String defaultDatabaseName){

        try {
            client = new MongoClient(host);
            this.defaultDatabaseName = defaultDatabaseName;
        }
        catch(Exception ex){
            throw new DatabaseException(ex);
        }
    }

    /**
     * list all Mongo Databases
     * @return List of database names
     */
    public List<String> allDatabases(){
        return client.getDatabaseNames();
    }

    /**
     * Explicitly close the mongo client
     */
    public void close(){
        if (client != null)
            client.close();
        client = null;
    }

    /**
     * Get a reference to the specified mongo database collection
     * @param databaseName
     * @param collectionName
     * @return
     */
    public MongoCollection getCollection(String databaseName, String collectionName) {
        Jongo db = this.getDatabase(databaseName);
        return db.getCollection(collectionName);
    }

    public MongoCollection getCollection(String collectionName){
        return this.getCollection(this.defaultDatabaseName, collectionName);
    }

    public DBCollection getMongoCollection(String databaseName, String collectionName) {
        DB db = client.getDB(databaseName);
        return db.getCollection(collectionName);
    }

    public DBCollection getMongoCollection(String collectionName) {
        return this.getMongoCollection(this.defaultDatabaseName, collectionName);
    }

    public GridFS getGridFS(String databaseName){
        DB db = client.getDB(databaseName);
        return new GridFS(db);
    }

    public GridFS getGridFS(){
        return this.getGridFS(this.defaultDatabaseName);
    }

    /**
     * Get a reference to the specified mongo database
     * @param name
     * @return
     */
    private Jongo getDatabase(String name){
        return new Jongo(client.getDB(name));
    }
}
