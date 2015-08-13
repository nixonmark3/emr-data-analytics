package emr.analytics.models.diagram;

import emr.analytics.models.definition.DefinitionType;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Jackson schema for Diagram.
 */
public class Diagram implements Serializable {

    private UUID id = null;
    private String name = "";
    private String description = "";
    private String owner = "";
    private Mode mode = Mode.OFFLINE;
    private TargetEnvironments targetEnvironment = TargetEnvironments.PYTHON;
    private int height = 1000;
    private int width = 1000;
    private int version = 0;

    private List<Wire> wires = new ArrayList<Wire>();
    private List<Block> blocks = new ArrayList<Block>();
    private List<DiagramConnector> inputs = new ArrayList<DiagramConnector>();
    private List<DiagramConnector> outputs = new ArrayList<DiagramConnector>();
    private List<PersistedOutput> persistedOutputs = new ArrayList<PersistedOutput>();
    private List<Diagram> diagrams = new ArrayList<Diagram>();
    private List<ParameterOverride> parameterOverrides = new ArrayList<ParameterOverride>();

    public Diagram(String name, String description, String owner, Mode mode){
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.mode = mode;
    }

    public Diagram(String name, String description, String owner) { this(name, description, owner, Mode.OFFLINE); }

    public Diagram(String name){
        this(name, "", "");
    }

    private Diagram() {}

    public static Diagram Create() {
        return new Diagram("New Diagram", "", "");
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the name of this Diagram.
     * @return diagram name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Diagram.
     * @param name diagram name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this Diagram.
     * @return diagram description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this Diagram.
     * @param description diagram description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the name of the owner of this Diagram.
     * @return diagram owner name
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the name of the owner of this Diagram.
     * @param owner diagram owner name
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Gets this diagram's mode
     * @return diagram's mode
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * Sets this diagram's mode
     * @param mode the mode to set this diagram to.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets this diagram's target environment
     * @return diagram's target environment
     */
    public TargetEnvironments getTargetEnvironment() {
        return this.targetEnvironment;
    }

    /**
     * Sets this diagram's target environment
     * @param targetEnvironment the target environment to set the diagram to.
     */
    public void setTargetEnvironment(TargetEnvironments targetEnvironment) {
        this.targetEnvironment = targetEnvironment;
    }

    /**
     * Returns a list of Wires that belong to this Diagram.
     * @return list of wires
     */
    public List<Wire> getWires() {
        return wires;
    }

    /**
     * Sets the Wires that belong to this Diagram.
     * @param wires list of wires
     */
    public void setWires(List<Wire> wires) {
        this.wires = wires;
    }

    /**
     * Returns a list of Blocks that belong to this Diagram.
     * @return list of blocks
     */
    public List<Block> getBlocks() {
        return blocks;
    }

    /**
     * Returns blocks by Definition Type
     * @return list of blocks
     */
    public List<Block> getBlocks(DefinitionType definitionType){
        return this.blocks
            .stream()
            .filter(b -> b.getDefinitionType().equals(definitionType))
            .collect(Collectors.toList());
    }

    /**
     * Returns the list of blocks that have at least one persisted output
     * @return list of blocks
     */
    public List<Block> getBlocksWithPersistedOutputs(){
        return this.blocks.stream().filter(b -> b.hasPersistedOutputs()).collect(Collectors.toList());
    }

    /**
     * Returns the specified block
     * @return Block
     */
    public Block getBlock(String name){
        return blocks.stream()
                .filter(b -> b.getName().equals(name))
                .findFirst()
                .get();
    }

    public Block getBlock(UUID id){
        return blocks.stream()
            .filter(b -> b.getId().equals(id))
            .findFirst()
            .get();
    }

    /**
     * Sets the Blocks that belong to this Diagram.
     * @param blocks list of blocks
     */
    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    /**
     * Returns a list of Groups that belong to this Diagram.
     * @return list of Groups
     */
    public List<Diagram> getDiagrams() {
        return diagrams;
    }

    /**
     * Sets the Diagrams that belong to this Diagram.
     * @param diagrams list of diagrams
     */
    public void setDiagrams(List<Diagram> diagrams) {
        this.diagrams = diagrams;
    }

    /**
     * Returns the version of this Diagram.
     * @return diagram version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version of this Diagram.
     * @param version diagram version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Retrieve a list of root level blocks
     */
    public List<Block> getRoot(){

        return this.blocks.stream()
                .filter(b -> b.getInputConnectors().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Retrieve the next set of blocks that spawn from the name of specified block
     */
    public List<Block> getNext(UUID blockId){

        List<UUID> ids = this.wires.stream()
                .filter(w -> w.getFrom_node().equals(blockId))
                .map(w -> w.getTo_node())
                .collect(Collectors.toList());

        return this.blocks.stream()
                .filter(b -> ids.contains(b.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve the wires that lead to the specified block
     */
    public List<Wire> getLeadingWires(UUID blockId){

        return this.wires.stream()
                .filter(w -> w.getTo_node().equals(blockId))
                .collect(Collectors.toList());
    }

    public List<Wire> getLeadingWires(UUID blockId, String connectorName){

        return this.wires.stream()
                .filter(w -> w.getTo_node().equals(blockId)
                        && w.getTo_connector().equals(connectorName))
                .collect(Collectors.toList());
    }

    public List<Wire> getOutputWires(UUID blockId){

        return this.wires.stream()
                .filter(w -> w.getFrom_node().equals(blockId))
                .collect(Collectors.toList());
    }

    public List<Wire> getOutputWires(UUID blockId, String connectorName){

        return this.wires.stream()
                .filter(w -> w.getFrom_node().equals(blockId)
                        && w.getFrom_connector().equals(connectorName))
                .collect(Collectors.toList());
    }

    /**
     * Add a new block
     */
    public void addBlock(Block block){

        // todo: verify block has a unique name

        this.blocks.add(block);
    }

    public boolean removeBlock(Block block){

        return this.blocks.remove(block);
    }

    /**
     * Add a new wire
     */
    public void addWire(Wire wire){

        // todo: verify valid wire

        this.wires.add(wire);
    }

    public boolean removeWire(Wire wire){

        return this.wires.remove(wire);
    }

    public void addDiagram(Diagram diagram){
        this.diagrams.add(diagram);
    }

    public List<DiagramConnector> getInputs(){
        return this.inputs;
    }

    public List<DiagramConnector> getOutputs(){
        return this.outputs;
    }

    public void addInput(DiagramConnector connector){ this.inputs.add(connector); }

    public void addOutput(DiagramConnector connector) { this.outputs.add(connector); }

    public List<PersistedOutput> getPersistedOutputs(){ return this.persistedOutputs; }

    public void addPersistedOutput(PersistedOutput persistedOutput){ this.persistedOutputs.add(persistedOutput); }

    public List<ParameterOverride> getParameterOverrides() {

        return parameterOverrides;
    }

    public void setParameterOverrides(List<ParameterOverride> parameterOverrides) {

        this.parameterOverrides = parameterOverrides;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
