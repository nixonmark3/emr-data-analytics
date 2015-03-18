package models.definition;

/**
 * MongoDB schema for Connector Definition.
 */
public class ConnectorDefinition {
    /**
     * Connector Definition constructor.
     * @param name connector name
     * @param dataType connector data type
     */
    public ConnectorDefinition(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }

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
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type of this Connector Definition.
     * @param dataType connector data type
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Hidden Jackson constructor.
     */
    private ConnectorDefinition() {}

    /**
     * Private members.
     */
    private String name;
    private String dataType;
}