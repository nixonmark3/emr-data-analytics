package emr.analytics.models.definition;

import java.util.ArrayList;
import java.util.List;

public class ParameterDefinition {
    private String name = null;
    private String type = null;
    private String value = null;
    private List<String> fieldOptions = new ArrayList<String>();
    private ParameterSource source = null;

    private ParameterDefinition() {}

    public ParameterDefinition(String name, String type, String value, List<String> fieldOptions, ParameterSource source) {
        this.name = name;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
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
