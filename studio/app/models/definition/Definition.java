package models.definition;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB Schema for Definition.
 */
public class Definition {
    /**
     * Returns the width of a Definition instance graphic.
     * @return definition instance graphic width
     */
    public int getW() { return w; }

    /**
     * Sets the width of a Definition instance graphic.
     * @param width definition instance graphic width
     */
    public void setW(int width) { this.w = width; }

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
    public List<ParameterDefinition> getParameters() { return parameters; }

    /**
     * Sets the list of Parameter Definitions that belong to this Definition.
     * @param parameters list of parameter definitions
     */
    public void setParameters(List<ParameterDefinition> parameters) {
        this.parameters = parameters;
    }

    /**
     * Retruns the list of Input Connectors that belong to this Definition.
     * @return list of input connectors
     */
    public List<ConnectorDefinition> getInputConnectors() { return inputConnectors; }

    /**
     * Sets the list of Input Connectors tha belong to this Definition.
     * @param inputConnectors list of input connectors
     */
    public void setInputConnectors(List<ConnectorDefinition> inputConnectors) {
        this.inputConnectors = inputConnectors;
    }

    /**
     * Returns the list of Output Connectors that belong to this Definition.
     * @return list of output connectors
     */
    public List<ConnectorDefinition> getOutputConnectors() { return outputConnectors; }

    /**
     * Sets the list of Output Connectors that belong to this Definition.
     * @param outputConnectors list of output connectors
     */
    public void setOutputConnectors(List<ConnectorDefinition> outputConnectors) {
        this.outputConnectors = outputConnectors;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Definition() {}

    /**
     * Private members.
     */
    private int w;
    private String description;
    private String name;
    private List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
    private List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
    private List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
}
