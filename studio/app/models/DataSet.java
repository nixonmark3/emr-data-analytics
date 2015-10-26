package models;

import org.bson.types.ObjectId;

/**
 * MongoDB schema for Data Set.
 */
public class DataSet {
    /**
     * Returns the unique object id that is available to each Mongo Document.
     * @return id of data set document
     */
    public ObjectId get_id() {
        return _id;
    }

    /**
     * Sets the unique object id that is available to each Mongo Document.
     * @param _id id of data set document
     */
    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    /**
     * Returns the name of this Data Set.
     * @return data set name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Data Set.
     * @param name data set name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Hidden Jackson constructor.
     */
    private DataSet() {}

    /**
     * Private members.
     */
    private ObjectId _id;
    private String name;
}
