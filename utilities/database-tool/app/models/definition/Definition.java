package models.definition;

import java.util.*;

public class Definition {
    public Definition(String name) {
        this.name = name;
        this.w = 200;
        this.parameters = new ArrayList<ParameterDefinition>();
        this.inputConnectors = new ArrayList<ConnectorDefinition>();
        this.outputConnectors = new ArrayList<ConnectorDefinition>();;
    }

    public int getW() { return w; }

    public void setW(int width) { this.w = width; }

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

    public List<ParameterDefinition> getParameters() { return parameters; }

    public void setParameters(List<ParameterDefinition> parameters) {
        this.parameters = parameters;
    }

    public List<ConnectorDefinition> getInputConnectors() { return inputConnectors; }

    public void setInputConnectors(List<ConnectorDefinition> inputConnectors) {
        this.inputConnectors = inputConnectors;
    }

    public List<ConnectorDefinition> getOutputConnectors() { return outputConnectors; }

    public void setOutputConnectors(List<ConnectorDefinition> outputConnectors) {
        this.outputConnectors = outputConnectors;
    }

    private int w;
    private String description;
    private String name;
    private List<ParameterDefinition> parameters;
    private List<ConnectorDefinition> inputConnectors;
    private List<ConnectorDefinition> outputConnectors;
}
