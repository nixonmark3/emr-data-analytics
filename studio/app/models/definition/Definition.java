package models.definition;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB Schema for Definition.
 */
public class Definition {
    /**
     * Definition constructor.
     * @param name definition name
     */
    public Definition(String name) {
        this.name = name;
        this.width = 200;
        this.parameterDefinitions = new ArrayList<ParameterDefinition>();
        this.inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        this.outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();;
    }

    /**
     * Returns the width of a Definition instance graphic.
     * @return definition instance graphic width
     */
    public int getWidth() { return width; }

    /**
     * Sets the width of a Definition instance graphic.
     * @param width definition instance graphic width
     */
    public void setWidth(int width) { this.width = width; }

    /**
     * Returns the name of this Definition.
     * @return definition name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Definition.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this Definition.
     * @return definition description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this Definition.
     * @param description definition description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the list of Parameter Definitions that belong to this Definition.
     * @return list of parameter definitions
     */
    public List<ParameterDefinition> getParameterDefinitions() { return parameterDefinitions; }

    /**
     * Sets the list of Parameter Definitions that belong to this Definition.
     * @param parameterDefinitions list of parameter definitions
     */
    public void setParameterDefinitions(List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    /**
     * Retruns the list of Input Connectors that belong to this Definition.
     * @return list of input connectors
     */
    public List<ConnectorDefinition> getInputConnectorDefinitions() { return inputConnectorDefinitions; }

    /**
     * Sets the list of Input Connectors tha belong to this Definition.
     * @param inputConnectorDefinitions list of input connectors
     */
    public void setInputConnectorDefinitions(List<ConnectorDefinition> inputConnectorDefinitions) {
        this.inputConnectorDefinitions = inputConnectorDefinitions;
    }

    /**
     * Returns the list of Output Connectors that belong to this Definition.
     * @return list of output connectors
     */
    public List<ConnectorDefinition> getOutputConnectorDefinitions() { return outputConnectorDefinitions; }

    /**
     * Sets the list of Output Connectors that belong to this Definition.
     * @param outputConnectorDefinitions list of output connectors
     */
    public void setOutputConnectorDefinitions(List<ConnectorDefinition> outputConnectorDefinitions) {
        this.outputConnectorDefinitions = outputConnectorDefinitions;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Definition() {}

    /**
     * Private members.
     */
    private int width;
    private String description;
    private String name;
    private List<ParameterDefinition> parameterDefinitions;
    private List<ConnectorDefinition> inputConnectorDefinitions;
    private List<ConnectorDefinition> outputConnectorDefinitions;
}
