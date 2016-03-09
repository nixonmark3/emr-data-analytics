package emr.analytics.models.diagram;

import emr.analytics.models.definition.ParameterDefinition;
import emr.analytics.models.definition.ParameterType;
import emr.analytics.models.definition.ValueType;

import java.io.Serializable;

/**
 * Jackson schema for Diagram Parameter.
 */
public class Parameter implements Serializable {
    private String name;
    private Object value;
    private ParameterType parameterType;
    private ValueType valueType;

    private Parameter() {}

    public Parameter(ParameterDefinition parameterDefinition){

        this.name = parameterDefinition.getName();
        this.parameterType = parameterDefinition.getParameterType();
        this.valueType = parameterDefinition.getValueType();
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

    public ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) { this.valueType = valueType; }
}
