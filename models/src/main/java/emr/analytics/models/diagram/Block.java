package emr.analytics.models.diagram;

import emr.analytics.models.definition.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Jackson schema for Diagram Block.
 */
public class Block implements Serializable {

    private DefinitionType definitionType;
    private UUID id;
    private String name;
    private String definition;
    private boolean configured;
    private boolean dirty;
    private int x;
    private int y;
    private int w;
    private List<Connector> inputConnectors = new ArrayList<Connector>();
    private List<Connector> outputConnectors = new ArrayList<Connector>();
    private List<Parameter> parameters = new ArrayList<Parameter>();

    private Block() { }

    public Block(UUID id, String name, int x, int y, Mode mode, Definition definition){

        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.definitionType = definition.getDefinitionType();
        this.definition = definition.getName();
        this.w = definition.getW();

        // reference the appropriate model
        ModeDefinition modeDefinition = definition.getModel(mode);

        this.inputConnectors = createConnectors(modeDefinition.getInputs());
        this.outputConnectors = createConnectors(modeDefinition.getOutputs());
        this.parameters = createParameters(modeDefinition.getParameters());
    }

    public Block(String name, int x, int y, Mode mode, Definition definition){
        this(UUID.randomUUID(), name, x, y, mode, definition);
    }

    public DefinitionType getDefinitionType() { return definitionType; }

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

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
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
     * Get the property that represents whether all parameters have been configured
     * @return boolean
     */
    public boolean getConfigured(){ return this.configured; }

    /**
     * Get the property that represents whether a parameter has been edited
     * @return boolean
     */
    public boolean getDirty() { return this.dirty; }

    /**
     * Sets the property that represents whether a parameter has been edited
     * @param dirty
     */
    public void setDirty(boolean dirty){ this.dirty = dirty; }

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
    public Optional<Parameter> getParameter(String name){
        return parameters.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }

    /**
     * Sets the list of Parameters that belong to this Block.
     * @param parameters list of parameters
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    private List<Connector> createConnectors(List<ConnectorDefinition> connectorDefinitions){

        List<Connector> connectors = new ArrayList<>();
        for (ConnectorDefinition connectorDefinition : connectorDefinitions)
            connectors.add(new Connector(connectorDefinition));

        return connectors;
    }

    private List<Parameter> createParameters(List<ParameterDefinition> parameterDefinitions){

        List<Parameter> parameters = new ArrayList<>();
        for (ParameterDefinition parameterDefinition : parameterDefinitions)
            parameters.add(new Parameter(parameterDefinition));

        return parameters;
    }

    public boolean setParameter(String name, Object value){

        Optional<Parameter> parameter = this.getParameter(name);
        if (!parameter.isPresent())
            return false;

        parameter.get().setValue(value);

        return true;
    }

    /**
     * Specifies whether this block contains any persisted outputs
     * @return boolean value that indicates whether there are any persisted outputs
     */
    public boolean hasPersistedOutputs(){

        return (!this.outputConnectors.stream().anyMatch(c -> c.getPersisted()));
    }

    public boolean hasInputConnector(String name){

        return this.hasConnector(name, this.inputConnectors);
    }

    public void addInputConnector(Connector connector){

        this.inputConnectors.add(connector);
    }

    public void addOutputConnector(Connector connector){

        this.outputConnectors.add(connector);
    }

    public Optional<Connector> getInputConnector(String name){

        return this.getConnector(name, this.inputConnectors);
    }

    public Optional<Connector> getOutputConnector(String name){

        return this.getConnector(name, this.outputConnectors);
    }

    private boolean hasConnector(String name, List<Connector> connectors){

        return (connectors.stream().anyMatch(c -> c.getName().equals(name)));
    }

    private Optional<Connector> getConnector(String name, List<Connector> connectors){

        return (connectors.stream().filter(c -> c.getName().equals(name)).findFirst());
    }

    public List<Connector> persistedOutputs(){

        return this.outputConnectors.stream().filter(c -> c.getPersisted()).collect(Collectors.toList());
    }
}