package emr.analytics.models.diagram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jackson schema for Diagram.
 */
public class Diagram implements Serializable {
    private String name = "";
    private String description = "";
    private String owner = "";
    private List<Wire> wires = new ArrayList<Wire>();
    private List<Block> blocks = new ArrayList<Block>();
    private int version = 0;

    private Diagram() {}

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
     * Returns the specified block
     * @return Block
     */
    public Block getBlock(String name){
        return blocks.stream()
                .filter(b -> b.getName().equals(name))
                .findFirst()
                .get();
    }

    /**
     * Sets the Blocks that belong to this Diagram.
     * @param blocks list of blocks
     */
    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    /**
     * Returns the version of this Diagram.
     * @return diagram version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version of this Diagram.
     * @param version diagram version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Retrieve a list of root level blocks
     */
    public List<Block> getRoot(){

        return this.blocks.stream()
                .filter(b -> b.getInputConnectors().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Retrieve the next set of blocks that spawn from the name of specified block
     */
    public List<Block> getNext(String name){

        List<String> names = this.wires.stream()
                .filter(w -> w.getFrom_node().equals(name))
                .map(w -> w.getTo_node())
                .collect(Collectors.toList());

        return this.blocks.stream()
                .filter(b -> names.contains(b.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve the wires that lead to the specified block
     */
    public List<Wire> getLeadingWires(String name){

        return this.wires.stream()
                .filter(w -> w.getTo_node().equals(name))
                .collect(Collectors.toList());
    }
}