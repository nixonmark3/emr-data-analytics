package models.definition;

/**
 * MongoDB schema for Connector Definition.
 */
public class ConnectorDefinition {
    /**
     * Returns the name of this Connector Definition.
     * @return connector name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Connector Definition.
     * @param name connector name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the data type of this Connector Definition.
     * @return connector data type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the data type of this Connector Definition.
     * @param type connector data type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Hidden Jackson constructor.
     */
    private ConnectorDefinition() {}

    /**
     * Private members.
     */
    private String name;
    private String type;
}