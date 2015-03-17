package models.definition;

import java.util.*;

public class Definition {
    public Definition(String name) {
        this.name = name;
        this.width = 200;
        this.parameterDefinitions = new ArrayList<ParameterDefinition>();
        this.inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        this.outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();;
    }

    public int getWidth() { return width; }

    public void setWidth(int width) { this.width = width; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ParameterDefinition> getParameterDefinitions() { return parameterDefinitions; }

    public void setParameterDefinitions(List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    public List<ConnectorDefinition> getInputConnectorDefinitions() { return inputConnectorDefinitions; }

    public void setInputConnectorDefinitions(List<ConnectorDefinition> inputConnectorDefinitions) {
        this.inputConnectorDefinitions = inputConnectorDefinitions;
    }

    public List<ConnectorDefinition> getOutputConnectorDefinitions() { return outputConnectorDefinitions; }

    public void setOutputConnectorDefinitions(List<ConnectorDefinition> outputConnectorDefinitions) {
        this.outputConnectorDefinitions = outputConnectorDefinitions;
    }

    private int width;
    private String description;
    private String name;
    private List<ParameterDefinition> parameterDefinitions;
    private List<ConnectorDefinition> inputConnectorDefinitions;
    private List<ConnectorDefinition> outputConnectorDefinitions;
}
