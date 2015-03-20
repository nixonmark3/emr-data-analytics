package models.diagram;

/**
 * MongoDB schema for Diagram Parameter.
 */
public class Parameter {
    /**
     * Returns the name of this Parameter.
     * @return parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Parameter.
     * @param name paramter name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the current value of this Parameter.
     * @return parameters current value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the current value of this Parameter.
     * @param value parameters current value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Hidden Jackson constructor
     */
    private Parameter() {}

    /**
     * Private members.
     */
    private String name;
    private String value;
}
