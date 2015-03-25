package models.definition;

/**
 * MongoDB schema for Parameter Definition.
 */
public class ParameterDefinition {
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
    public String getType() {
        return type;
    }

    /**
     * Sets the data type of this Parameter Definition.
     * @param type parameter definition data type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the default value of this Parameter Definition.
     * @return parameter definition default value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the default value of this Parameter Definition.
     * @param value parameter definition default value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Hidden Jackson constructor.
     */
    private ParameterDefinition() {}

    /**
     * Private members.
     */
    private String name;
    private String type;
    private String value;
}
