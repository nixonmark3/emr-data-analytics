package models.diagram;

/**
 * MongoDB schema for a Diagram Wire.
 */
public class Wire {
    /**
     * Wire constructor.
     * @param from_node
     * @param from_connectorIndex
     * @param to_node
     * @param to_connectorIndex
     */
    public Wire(String from_node, int from_connectorIndex, String to_node, int to_connectorIndex) {
        this.from_node = from_node;
        this.from_connectorIndex = from_connectorIndex;
        this.to_node = to_node;
        this.to_connectorIndex = to_connectorIndex;
    }

    /**
     * Returns the name of the from node of this Wire.
     * @return name of from node
     */
    public String getFrom_node() {
        return from_node;
    }

    /**
     * Sets the name of the from node of this Wire.
     * @param from_node name of from node
     */
    public void setFrom_node(String from_node) {
        this.from_node = from_node;
    }

    /**
     * Returns the index of the from node connector of this Wire.
     * @return index of from node connector
     */
    public int getFrom_connectorIndex() {
        return from_connectorIndex;
    }

    /**
     * Sets the index of the from node connector of this Wire.
     * @param from_connectorIndex index of from node connector
     */
    public void setFrom_connectorIndex(int from_connectorIndex) {
        this.from_connectorIndex = from_connectorIndex;
    }

    /**
     * Returns the name of the to node of this Wire.
     * @return name of to node
     */
    public String getTo_node() {
        return to_node;
    }

    /**
     * Sets the name of the to node of this Wire.
     * @param to_node name of node
     */
    public void setTo_node(String to_node) {
        this.to_node = to_node;
    }

    /**
     * Returns the index of the to node connector of this Wire.
     * @return index of to node connector
     */
    public int getTo_connectorIndex() {
        return to_connectorIndex;
    }

    /**
     * Sets the index of the to node connector of this Wire.
     * @param to_connectorIndex index of to node connector
     */
    public void setTo_connectorIndex(int to_connectorIndex) {
        this.to_connectorIndex = to_connectorIndex;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Wire() {}

    /**
     * Private members.
     */
    private String from_node;
    private int from_connectorIndex;
    private String to_node;
    private int to_connectorIndex;
}