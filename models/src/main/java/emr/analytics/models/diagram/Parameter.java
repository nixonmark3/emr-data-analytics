package emr.analytics.models.diagram;

import emr.analytics.models.definition.ParameterDefinition;

import java.io.Serializable;

/**
 * Jackson schema for Diagram Parameter.
 */
public class Parameter implements Serializable {
    private String name = "";
    private Object value = null;
    private boolean collected = false;

    private Parameter() {}

    public Parameter(ParameterDefinition parameterDefinition){

        this.name = parameterDefinition.getName();
        this.value = parameterDefinition.getValue();
    }

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
    public Object getValue() {
        return value;
    }

    /**
     * Sets the current value of this Parameter.
     * @param value parameters current value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Determines if this parameter has been collected.
     * @return boolean is collected
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Sets the collected field of a parameter.
     * @param collected boolean is collected
     */
    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}
