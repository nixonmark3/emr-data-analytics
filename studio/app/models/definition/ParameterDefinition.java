package models.definition;

/**
 * MongoDB schema for Parameter Definition.
 */
public class ParameterDefinition {
    /**
     * Parameter Definition constructor.
     * @param name parameter definition name
     * @param dataType parameter definition data type
     * @param defaultValue parameter definition default value
     */
    public ParameterDefinition(String name, String dataType, String defaultValue) {
        this.name = name;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the name of this Parameter Definition.
     * @return parameter definition name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Parameter Definition.
     * @param name parameter definition name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the data type of this Parameter Definition.
     * @return parameter definition data type
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type of this Parameter Definition.
     * @param dataType parameter definition data type
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Returns the default value of this Parameter Definition.
     * @return parameter definition default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value of this Parameter Definition.
     * @param defaultValue parameter definition default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Hidden Jackson constructor.
     */
    private ParameterDefinition() {}

    /**
     * Private members.
     */
    private String name;
    private String dataType;
    private String defaultValue;
}
