package models.definition;

import org.bson.types.ObjectId;

import java.util.*;

/**
 * MongoDB schema for Category.
 */
public class Category {
    /**
     * Category constructor.
     * @param name category name
     */
    public Category(String name) {
        this.name = name;
        this.definitions = new ArrayList<Definition>();
    }

    /**
     * Returns the name of this Category.
     * @return category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Category.
     * @param name category name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the list of Definitions that belong to this Category.
     * @return definitions list
     */
    public List<Definition> getDefinitions() {
        return definitions;
    }

    /**
     * Sets the list of Definitions that belong to this Category.
     * @param definitions definitions list
     */
    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    /**
     * Returns the unique object id that is available to each Mongo Document.
     * @return id of diagram document
     */
    public ObjectId get_id() {
        return _id;
    }

    /**
     * Sets the unique object id that is available to each Mongo Document.
     * @param _id id of diagram document
     */
    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Category() {}

    /**
     * Private members.
     */
    private ObjectId _id;
    private String name;
    private List<Definition> definitions;
}
