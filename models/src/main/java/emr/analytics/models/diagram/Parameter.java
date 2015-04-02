package emr.analytics.models.diagram;

import java.io.Serializable;

/**
 * Jackson schema for Diagram Parameter.
 */
public class Parameter implements Serializable {
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
     * Returns the type of this Parameter.
     * @return parameter type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this Parameter.
     * @param type parameter type
     */
    public void setType(String type) {
        this.type = type;
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
    private String type; // todo remove this property as it is not required on instance
}
