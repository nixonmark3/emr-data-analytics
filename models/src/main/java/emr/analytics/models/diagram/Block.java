package emr.analytics.models.diagram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Jackson schema for Diagram Block.
 */
public class Block implements Serializable {
    /**
     * Returns the name of this Block.
     * @return block name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Block.
     * @param name block name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the definition that this Block is based on.
     * @return block definition name
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Sets the name of the definition that this Block is based on.
     * @param definition block definition name
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * Returns the state of this Block.
     * @return block state
     */
    public int getState() {
        return state;
    }

    /**
     * Sets the state of this Block.
     * @param state block state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Sets the x position of this Block in Diagram.
     * @return x position of block in diagram
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x position of this Block in Diagram.
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y position of this block in Diagram.
     * @return y position of block in diagram
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y position of this Block in Diagram.
     * @param y y position of block in diagram
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Returns the width of this Block.
     * @return block width
     */
    public int getW() {
        return w;
    }

    /**
     * Sets the width of this Block.
     * @param w block width
     */
    public void setW(int w) {
        this.w = w;
    }

    /**
     * Returns a list of input Connectors tha belong to this Block.
     * @return list of connectors
     */
    public List<Connector> getInputConnectors() {
        return inputConnectors;
    }

    /**
     * Sets the list of input Connectors that belong to this Block.
     * @param inputConnectors list of connectors
     */
    public void setInputConnectors(List<Connector> inputConnectors) {
        this.inputConnectors = inputConnectors;
    }

    /**
     * Returns a list of output Connectors that belong to this Block.
     * @return list of connectors
     */
    public List<Connector> getOutputConnectors() {
        return outputConnectors;
    }

    /**
     * Sets ths list of output Connectors that belong to this Block.
     * @param outputConnectors list of connectors
     */
    public void setOutputConnectors(List<Connector> outputConnectors) {
        this.outputConnectors = outputConnectors;
    }

    /**
     * Returns the list of Parameters that belong to this Block.
     * @return list of parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Returns the specified parameter
     * @return Parameter
     */
    public Parameter getParameter(String name){
        return parameters.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .get();
    }

    /**
     * Sets the list of Parameters that belong to this Block.
     * @param parameters list of parameters
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Block() { }

    /**
     * Private members.
     */
    private boolean configured;
    private String name;
    private String definition;
    private int state;
    private int x;
    private int y;
    private int w;
    private List<Connector> inputConnectors = new ArrayList<Connector>();
    private List<Connector> outputConnectors = new ArrayList<Connector>();
    private List<Parameter> parameters = new ArrayList<Parameter>();

    /**
     * Verifies that this block has been configured
     */
    public boolean isConfigured(){

        // todo: enhance configuration criteria
        // configuration criteria is that all parameters have a value

        return (!this.parameters.stream().anyMatch(p -> p.getValue().equals(null)));
    }
}