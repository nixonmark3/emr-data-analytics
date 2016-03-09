package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.*;

import java.util.*;

/**
 * Transforming offline diagrams into online diagrams
 */
public class DiagramTransformer {

    private Map<String, Definition> definitions;

    public DiagramTransformer(Map<String, Definition> definitions) {
        this.definitions = definitions;
    }

    /**
     * Transforms the specified offline diagram into an online diagram
     * @param offline
     * @return
     */
    public Diagram transform(Diagram offline){
        return (new Transformer(offline).transform());
    }

    private class Transformer {

        private final String terminatorName = "Output";

        // the offline diagram being transformed
        private Diagram offline;
        // the set of distinct block names that have been used to build the online diagram
        private HashSet<String> blockNames;
        // the terminating block definition
        private Definition terminator;

        public Transformer(Diagram offline){
            this.offline = offline;
            terminator = definitions.get(this.terminatorName);
        }

        public Diagram transform(){

            // instantiate the set of online block names
            blockNames = new HashSet<>();

            // create a new online diagram
            Diagram online = new Diagram(offline.getName(),
                    offline.getDescription(),
                    offline.getOwner(),
                    Mode.ONLINE);
            // the default online target environment is pyspark
            online.setTargetEnvironment(TargetEnvironments.PYSPARK);

            // capture the offline diagam's id, width, and height
            online.setId(offline.getId());
            online.setWidth(offline.getWidth());
            online.setHeight(offline.getHeight());

            // iterate over offline blocks to find model block
            int modelCount = 0;
            for(Block block : offline.getBlocks()){

                // capture each block's outputs that are persisted from offline to online
                for (Connector output : block.persistedOutputs())
                    online.addPersistedOutput(new PersistedOutput(block.getId(), output.getName(), output.getType()));

                // create a model version of the online block
                if (block.getDefinitionType().equals(DefinitionType.MODEL)) {

                    // create a new online block
                    Block onlineBlock = this.createOnlineBlock(block);
                    // add it to the diagram
                    online.addBlock(onlineBlock);

                    // follow the offline diagram to its root (collecting blocks along the way)
                    int index = 0;
                    for (Connector connector : onlineBlock.getInputConnectors()) {

                        if (block.hasInputConnector(connector.getName())) {

                            for (Wire wire : offline.getInputWires(block.getId(), connector.getName())) {

                                Wire onlineWire = new Wire(wire.getFrom_node(),
                                        wire.getFrom_connector(),
                                        wire.getFrom_connectorIndex(),
                                        onlineBlock.getId(),
                                        connector.getName(),
                                        index);

                                this.addInputPath(onlineWire, offline, online);
                            }
                        }

                        index++;
                    }

                    // add a terminating block to the output of the model block
                    UUID blockId = UUID.fromString(String.format("00000000-0000-0000-0000-%12s",
                            Integer.toHexString(modelCount)).replace(" ", "0"));

                    Block terminatingBlock = this.createBlock(blockId,
                            this.generateBlockName(this.terminator.getName()),
                            onlineBlock.getX(),
                            (onlineBlock.getY() + 150),
                            this.terminator);

                    this.applyParameterOverrides(terminatingBlock);
                    online.addBlock(terminatingBlock);

                    online.addWire(new Wire(
                            onlineBlock.getId(),
                            onlineBlock.getOutputConnectors().get(0).getName(),
                            0,
                            terminatingBlock.getId(),
                            terminatingBlock.getInputConnectors().get(0).getName(),
                            0));

                    modelCount++;
                }
            }

            return online;
        }

        private Block createOnlineBlock(Block block){

            // reference the block's name
            String name = block.getName();

            // retrieve the definition
            Definition definition = definitions.get(block.getDefinition());
            if (definition.getMode() == Mode.OFFLINE && definition.getComplement() != null) {
                // get the complements definition and create a new unique name based on the online definition
                definition = definitions.get(definition.getComplement());
                name = this.generateBlockName(definition.getName());
            }

            // create online block
            Block onlineBlock = createBlock(block.getId(),
                    name,
                    block.getX(),
                    block.getY(),
                    definition);

            // transfer parameter values from offline to online
            for(Parameter parameter : block.getParameters()) {
                Optional<Parameter> onlineParameter = onlineBlock.getParameter(parameter.getName());
                if (onlineParameter.isPresent())
                    onlineParameter.get().setValue(parameter.getValue());
            }

            if (definition.getOnlineDefinition() != null)
                this.applyParameterOverrides(onlineBlock);

            return onlineBlock;
        }

        private Block createBlock(UUID id, String name, int x, int y, Definition definition){
            blockNames.add(name);
            return new Block(id, name, x, y, Mode.ONLINE, definition);
        }

        /**
         * Generate a unique block name based on the definition name
         * @param definitionName
         * @return
         */
        private String generateBlockName(String definitionName){

            String name = definitionName;
            int index = 1;
            while(blockNames.contains(name)){
                name = definitionName + index;
                index++;
            };

            return name;
        }

        /**
         * Add the specified wire and associated input block to the destination diagram
         * (based on its configuration in the source diagram)
         * @param wire: wire from the source diagram
         * @param source: the source diagram
         * @param destination: the destination diagram
         */
        private void addInputPath(Wire wire, Diagram source, Diagram destination){

            UUID blockId = wire.getFrom_node();
            Block block = source.getBlock(blockId);
            String blockName = block.getName();

            if (!blockNames.contains(blockName)){

                Block onlineBlock = this.createOnlineBlock(block);
                destination.addBlock(onlineBlock);
            }

            destination.addWire(wire);

            for (Wire inputWire : source.getInputWires(blockId))
                this.addInputPath(new Wire(inputWire), source, destination);
        }

        /**
         * for a new online block, capture previously configured parameter values
         * @param block: an online block
         */
        private void applyParameterOverrides(Block block) {

            List<ParameterOverride> overrides = this.offline.getParameterOverrides();
            if (overrides != null) {

                overrides.stream()
                        .filter(p -> p.get_blockId().equals(block.getId()))
                        .forEach((override) -> {

                            Optional<Parameter> parameterToOverride = block.getParameter(override.get_name());

                            if (parameterToOverride.isPresent()) {

                                parameterToOverride.get().setValue(override.get_value());
                            }
                        });
            }
        }
    }
}
