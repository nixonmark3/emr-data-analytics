package emr.analytics.models.definition;

public class ParameterDefinition {
    private String name = null;
    private String type = null;
    private String value = null;
    private ParameterOptions options = null;

    private ParameterDefinition() {}

    public ParameterDefinition(String name, String type, String value, ParameterOptions options) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.options = options;
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

    public ParameterOptions getOptions() {
        return options;
    }

    public void setOptions(ParameterOptions options) {
        this.options = options;
    }
}
