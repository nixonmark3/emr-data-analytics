package models.diagram;

import org.bson.types.ObjectId;

import java.util.*;

public class Diagram {
    public Diagram(String name) {
        this.name = name;
        this.wires = new ArrayList<Wire>();
        this.blocks = new ArrayList<Block>();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Wire> getWires() {
        return wires;
    }

    public void setWires(List<Wire> wires) {
        this.wires = wires;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    private ObjectId _id;
    private String name;
    private String description;
    private String owner;
    private List<Wire> wires;
    private List<Block> blocks;
}
