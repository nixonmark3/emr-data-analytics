package emr.analytics.models.definition;

import java.util.List;

public class ParameterDefinition {
    private String name;
    private ParameterType parameterType;
    private ValueType valueType;
    private Object value;
    private List<String> fieldOptions;
    private ParameterSource source;

    private ParameterDefinition() {}

    public ParameterDefinition(String name, ParameterType parameterType, ValueType valueType, Object value, List<String> fieldOptions, ParameterSource source) {
        this.name = name;
        this.parameterType = parameterType;
        this.valueType = valueType;
        this.value = value;
        this.fieldOptions = fieldOptions;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<String> getFieldOptions() {
        return fieldOptions;
    }

    public void setFieldOptions(List<String> fieldOptions) {
        this.fieldOptions = fieldOptions;
    }

    public ParameterSource getSource() {
        return source;
    }

    public void setSource(ParameterSource source) {
        this.source = source;
    }
}
