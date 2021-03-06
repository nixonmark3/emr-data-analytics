package emr.analytics.models.diagram;

import java.io.Serializable;
import java.util.UUID;

/**
 * Jackson schema for a Diagram Wire.
 */
public class Wire implements Serializable {
    /**
     * Returns the name of the from node of this Wire.
     * @return name of from node
     */
    public UUID getFrom_node() {
        return from_node;
    }

    /**
     * Sets the name of the from node of this Wire.
     * @param from_node name of from node
     */
    public void setFrom_node(UUID from_node) {
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
    public UUID getTo_node() {
        return to_node;
    }

    /**
     * Sets the name of the to node of this Wire.
     * @param to_node name of node
     */
    public void setTo_node(UUID to_node) {
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
     * Returns the name of the from node connector of this Wire.
     * @return name of from node connector
     */
    public String getFrom_connector() {
        return from_connector;
    }

    /**
     * Sets the name of the from node connector of this Wire.
     * @param from_connector name of from node connector
     */
    public void setFrom_connector(String from_connector) {
        this.from_connector = from_connector;
    }

    /**
     * Returns the name of the to node connector of this Wire.
     * @return name of to node connector
     */
    public String getTo_connector() {
        return to_connector;
    }

    /**
     * Sets the name of the to node connector of this Wire.
     * @param to_connector name of to node connector
     */
    public void setTo_connector(String to_connector) {
        this.to_connector = to_connector;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Wire() {}

    public Wire(UUID from, String from_connector, int from_index, UUID to, String to_connector, int to_index){

        this.from_node = from;
        this.from_connector = from_connector;
        this.from_connectorIndex = from_index;
        this.to_node = to;
        this.to_connector = to_connector;
        this.to_connectorIndex = to_index;
    }

    /**
     * Private members.
     */
    private UUID from_node;
    private int from_connectorIndex;
    private String from_connector;
    private UUID to_node;
    private int to_connectorIndex;
    private String to_connector;
}