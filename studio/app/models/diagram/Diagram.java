package models.diagram;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB schema for Diagram.
 */
public class Diagram {
    /**
     * Diagram constructor.
     * @param name name of the diagram
     */
    public Diagram(String name) {
        this.name = name;
        this.wires = new ArrayList<Wire>();
        this.blocks = new ArrayList<Block>();
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
     * Returns the name of this Diagram.
     * @return diagram name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Diagram.
     * @param name diagram name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this Diagram.
     * @return diagram description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this Diagram.
     * @param description diagram description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the name of the owner of this Diagram.
     * @return diagram owner name
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the name of the owner of this Diagram.
     * @param owner diagram owner name
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Returns a list of Wires that belong to this Diagram.
     * @return list of wires
     */
    public List<Wire> getWires() {
        return wires;
    }

    /**
     * Sets the Wires that belong to this Diagram.
     * @param wires list of wires
     */
    public void setWires(List<Wire> wires) {
        this.wires = wires;
    }

    /**
     * Returns a list of Blocks that belong to this Diagram.
     * @return list of blocks
     */
    public List<Block> getBlocks() {
        return blocks;
    }

    /**
     * Sets the Blocks that belong to this Diagram.
     * @param blocks list of blocks
     */
    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Diagram() {}

    /**
     * Private members.
     */
    private ObjectId _id;
    private String name;
    private String description;
    private String owner;
    private List<Wire> wires;
    private List<Block> blocks;
}