package services;

import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.*;

import java.util.*;

public class DiagramCompiler {

    private HashSet<String> _onlineBlocks;
    private HashMap<String, String> _dataSources;

    private HashMap<String, Definition> _definitions;

    private Definition terminatingDefinition;

    public DiagramCompiler(HashMap<String, Definition> definitions){
        _definitions = definitions;

        terminatingDefinition = definitions.get("RESTPost");
    }

    public Diagram compile(Diagram offline){

        Diagram online = new Diagram(offline.getName(),
            offline.getDescription(),
            offline.getOwner(),
            Mode.ONLINE);

        _onlineBlocks = new HashSet<>();
        _dataSources = new HashMap<>();

        // iterate over offline blocks to find model block
        for(Block block : offline.getBlocks(DefinitionType.MODEL)){

            Block onlineBlock = this.createOnlineBlock(block.getName(), block);

            // add online block to diagram
            online.addBlock(onlineBlock);

            // follow the offline diagram to its root (collecting blocks along the way)
            int index = 0;
            for(Connector connector : onlineBlock.getInputConnectors()){

                if (block.hasInputConnector(connector.getName())){

                    for(Wire wire : offline.getLeadingWires(block.getId(), connector.getName())){

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
            Block postBlock = this.createOnlineBlock(UUID.randomUUID(),
                this.generateBlockName(this.terminatingDefinition.getName()),
                0,
                onlineBlock.getX(),
                (onlineBlock.getY() + 120),
                this.terminatingDefinition);

            online.addBlock(postBlock);

            online.addWire(new Wire(
                onlineBlock.getId(),
                onlineBlock.getOutputConnectors().get(0).getName(),
                0,
                postBlock.getId(),
                postBlock.getInputConnectors().get(0).getName(),
                0));
        }

        return online;
    }

    private Block createOnlineBlock(String name, Block block){

        // retrieve the definition
        Definition definition = _definitions.get(block.getDefinition());

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

        return onlineBlock;
    }

    private Block createOnlineBlock(UUID id, String name, int state, int x, int y, Definition definition){

        _onlineBlocks.add(name);
        return new Block(id, name, state, x, y, Mode.ONLINE, definition);
    }

    private String generateBlockName(String definitionName){

        String name = "";
        int index = 1;
        do{
            name = definitionName + index;
            index++;
        }while(_onlineBlocks.contains(name));

        return name;
    }

    private void addLeadingPath(Wire wire, Diagram source, Diagram destination){

        UUID blockId = wire.getFrom_node();
        Block block = source.getBlock(blockId);
        String blockName = block.getName();

        if (!_onlineBlocks.contains(blockName)){

            Block onlineBlock = this.createOnlineBlock(blockName, block);
            destination.addBlock(onlineBlock);
        }

        destination.addWire(wire);

        for (Wire leadingWire : source.getLeadingWires(blockId))
            this.addLeadingPath(leadingWire, source, destination);
    }
}
