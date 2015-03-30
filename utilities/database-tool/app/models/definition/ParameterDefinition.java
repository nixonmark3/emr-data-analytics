package models.definition;

public class ParameterDefinition {
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
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String name;

    public ParameterOptions getOptions() {
        return options;
    }

    public void setOptions(ParameterOptions options) {
        this.options = options;
    }

    private String type;
    private String value;
    private ParameterOptions options;
}
