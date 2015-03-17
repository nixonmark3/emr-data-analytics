package models.diagram;

public class Wire {

    public Wire(String from_node, int from_connectorIndex, String to_node, int to_connectorIndex) {
        this.from_node = from_node;
        this.from_connectorIndex = from_connectorIndex;
        this.to_node = to_node;
        this.to_connectorIndex = to_connectorIndex;
    }

    public String getFrom_node() {
        return from_node;
    }

    public void setFrom_node(String from_node) {
        this.from_node = from_node;
    }

    public int getFrom_connectorIndex() {
        return from_connectorIndex;
    }

    public void setFrom_connectorIndex(int from_connectorIndex) {
        this.from_connectorIndex = from_connectorIndex;
    }

    public String getTo_node() {
        return to_node;
    }

    public void setTo_node(String to_node) {
        this.to_node = to_node;
    }

    public int getTo_connectorIndex() {
        return to_connectorIndex;
    }

    public void setTo_connectorIndex(int to_connectorIndex) {
        this.to_connectorIndex = to_connectorIndex;
    }

    private String from_node;
    private int from_connectorIndex;
    private String to_node;
    private int to_connectorIndex;
}
