package controllers;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import play.mvc.Controller;

import plugins.MongoDBPlugin;

/**
 * Base class for all controllers.
 */
public abstract class ControllerBase extends Controller {
    /**
     * Returns the specified MongoDB collection.
     * @return mongo collection
     */
    protected static MongoCollection getMongoCollection(String collectionName) {
        MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();
        Jongo db = mongoPlugin.getJongoDBInstance(mongoPlugin.getStudioDatabaseName());
        if (db == null) {
            return null;
        }
        return db.getCollection(collectionName);
    }

    /**
     * Returns the specified MongoDB collection from the specified database.
     * @return mongo collection
     */
    protected static MongoCollection getMongoCollection(String databaseName, String collectionName) {
        MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();
        Jongo db = mongoPlugin.getJongoDBInstance(databaseName);
        if (db == null) {
            return null;
        }
        return db.getCollection(collectionName);
    }
}
