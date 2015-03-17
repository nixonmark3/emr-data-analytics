package models.definition;

public class ParameterDefinition {
    public ParameterDefinition(String name, DataType dataType, String defaultValue) {
        this.name = name;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    private String name;
    private DataType dataType;
    private String defaultValue;
}
