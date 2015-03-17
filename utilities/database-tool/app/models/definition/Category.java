package models.definition;

import org.bson.types.ObjectId;

import java.util.*;

public class Category {
    public Category(String name) {
        this.name = name;
        this.definitions = new ArrayList<Definition>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    private ObjectId _id;
    private String name;
    private List<Definition> definitions;
}
