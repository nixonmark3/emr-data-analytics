package models.diagram;

/**
 * MongoDB schema for Diagram Connector.
 */
public class Connector {
    /**
     * Connector constructor.
     * @param name name of connector
     * @param type specifies the direction of the connector
     * @param position determines the position of a connector
     */
    public Connector(String name, String type, String position) {
        this.name = name;
        this.type = type;
        this.position = position;
    }

    /**
     * Returns the name of this Connector.
     * @return connector name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Connector.
     * @param name connector name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type (data direction) of this Connector.
     * @return data direction of connector
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type (data direction) of this Connector.
     * @param type data direction of connector
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the position of this Connector as it will appear on it parent Block.
     * @return position of connector
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the position of this Connector as it will appear on it parent Block.
     * @param position position of connector
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Connector() {}

    /**
     * Private members.
     */
    private String type;
    private String name;
    private String position;
}