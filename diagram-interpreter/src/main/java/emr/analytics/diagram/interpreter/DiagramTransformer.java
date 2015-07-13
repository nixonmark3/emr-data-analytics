package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.DefinitionType;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.*;

import java.util.*;

/**
 * Transforming offline diagrams into online diagrams
 */
public class DiagramTransformer {

    private HashMap<String, Definition> definitions;

    public DiagramTransformer(HashMap<String, Definition> definitions) {

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

        private Diagram offline;

        private HashSet<String> onlineBlocks;
        private Definition terminatingDefinition;
        private List<ParameterOverride> parameterOverrides = null;

        public Transformer(Diagram offline){
            this.offline = offline;
            terminatingDefinition = definitions.get("RESTPost");
        }

        public Diagram transform(){

            parameterOverrides = offline.getParameterOverrides();

            Diagram online = new Diagram(offline.getName(),
                    offline.getDescription(),
                    offline.getOwner(),
                    Mode.ONLINE);

            // capture the offline diagam's id, width, and height
            online.setId(offline.getId());
            online.setWidth(offline.getWidth());
            online.setHeight(offline.getHeight());

            onlineBlocks = new HashSet<>();

            // iterate over offline blocks to find model block
            int modelCount = 0;
            for(Block block : offline.getBlocks()){

                for (Connector output : block.persistedOutputs())
                    online.addPersistedOutput(new PersistedOutput(block.getId(), output.getName(), output.getType()));

                if (block.getDefinitionType().equals(DefinitionType.MODEL)) {

                    Block onlineBlock = this.createOnlineBlock(block.getName(), block);

                    // add online block to diagram
                    online.addBlock(onlineBlock);

                    // follow the offline diagram to its root (collecting blocks along the way)
                    int index = 0;
                    for (Connector connector : onlineBlock.getInputConnectors()) {

                        if (block.hasInputConnector(connector.getName())) {

                            for (Wire wire : offline.getLeadingWires(block.getId(), connector.getName())) {

                                Wire onlineWire = new Wire(wire.getFrom_node(),
                                        wire.getFrom_connector(),
                                        wire.getFrom_connectorIndex(),
                                        onlineBlock.getId(),
                                        connector.getName(),
                                        index);

                                this.addLeadingPath(onlineWire, offline, online);
                            }
                        }

                        index++;
                    }

                    // add a result block to the output of the model block
                    UUID blockId = UUID.fromString(String.format("00000000-0000-0000-0000-%12s",
                            Integer.toHexString(modelCount)).replace(" ", "0"));
                    Block postBlock = this.createOnlineBlock(blockId,
                            this.generateBlockName(this.terminatingDefinition.getName()),
                            0,
                            onlineBlock.getX(),
                            (onlineBlock.getY() + 120),
                            this.terminatingDefinition);

                    this.applyParameterOverrides(postBlock);

                    online.addBlock(postBlock);

                    online.addWire(new Wire(
                            onlineBlock.getId(),
                            onlineBlock.getOutputConnectors().get(0).getName(),
                            0,
                            postBlock.getId(),
                            postBlock.getInputConnectors().get(0).getName(),
                            0));

                    modelCount++;
                }
            }

            return online;
        }

        private Block createOnlineBlock(String name, Block block){

            // retrieve the definition
            Definition definition = definitions.get(block.getDefinition());

            // create online block
            Block onlineBlock = createOnlineBlock(block.getId(),
                    name,
                    block.getState(),
                    block.getX(),
                    block.getY(),
                    definition);

            // set collected parameter values
            for(Parameter parameter : block.getParameters()){

                if (parameter.isCollected()) {

                    Optional<Parameter> onlineParameter = onlineBlock.getParameter(parameter.getName());
                    if (onlineParameter.isPresent()) {

                        onlineParameter.get().setValue(parameter.getValue());
                    }
                }
            }

            this.applyParameterOverrides(onlineBlock);

            return onlineBlock;
        }

        private Block createOnlineBlock(UUID id, String name, int state, int x, int y, Definition definition){

            onlineBlocks.add(name);
            return new Block(id, name, state, x, y, Mode.ONLINE, definition);
        }

        private String generateBlockName(String definitionName){

            String name = "";
            int index = 1;
            do{
                name = definitionName + index;
                index++;
            }while(onlineBlocks.contains(name));


            return name;
        }

        private void addLeadingPath(Wire wire, Diagram source, Diagram destination){

            UUID blockId = wire.getFrom_node();
            Block block = source.getBlock(blockId);
            String blockName = block.getName();

            if (!onlineBlocks.contains(blockName)){

                Block onlineBlock = this.createOnlineBlock(blockName, block);
                destination.addBlock(onlineBlock);
            }

            destination.addWire(wire);

            for (Wire leadingWire : source.getLeadingWires(blockId))
                this.addLeadingPath(leadingWire, source, destination);
        }

        private void applyParameterOverrides(Block block) {

            if (parameterOverrides != null) {

                parameterOverrides.stream()
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
