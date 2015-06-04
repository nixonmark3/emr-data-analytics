package services;

import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DiagramCompiler {

    private HashSet<String> _onlineBlocks;
    private HashMap<String, String> _dataSources;

    private HashMap<String, Definition> _definitions;

    private Definition sourceBlock;
    private Definition sinkBlock;

    public DiagramCompiler(HashMap<String, Definition> definitions){
        _definitions = definitions;

        sourceBlock = definitions.get("PollingStream");
        sinkBlock = definitions.get("RESTPost");
    }

    public Diagram compile(Diagram offline){

        Diagram online = new Diagram(offline.getName(),
            offline.getDescription(),
            offline.getOwner());

        _onlineBlocks = new HashSet<>();
        _dataSources = new HashMap<>();

        // iterate over offline blocks to find model block
        for(Block block : offline.getBlocks()){

            Definition offlineDefinition = _definitions.get(block.getDefinition());

            if (offlineDefinition.hasOnlineComplement()){

                // reference online definition
                Definition onlineDefinition = _definitions.get(offlineDefinition.getOnlineComplement());

                // create online block
                Block onlineBlock = this.createBlock(onlineDefinition,
                        block.getState(),
                        block.getX(),
                        block.getY());
                // reference offline complement
                onlineBlock.setOfflineComplement(block.getUniqueName());

                // set collected parameter values
                for(Parameter parameter : block.getParameters()){

                    // todo: bad assumption that model contains all block parameters
                    if (parameter.isCollected())
                        onlineBlock.setParameter(parameter.getName(), parameter.getValue());
                }

                // add online block to diagram
                online.addBlock(onlineBlock);

                // follow the offline diagram to its root (collecting blocks along the way)
                int index = 0;
                boolean sourceConnector = false;
                for(Connector connector : onlineBlock.getInputConnectors()){

                    String connectorName = connector.getName();
                    if (connectorName.toLowerCase().equals("x"))
                        sourceConnector = true;

                    if (block.hasInputConnector(connector.getName())){

                        for(Wire wire : offline.getLeadingWires(block.getUniqueName(), connector.getName())){

                            Wire onlineWire = new Wire(wire.getFrom_node(),
                                wire.getFrom_connector(),
                                wire.getFrom_connectorIndex(),
                                onlineBlock.getUniqueName(),
                                connector.getName(),
                                index);

                            this.addLeadingPath(onlineWire, offline, online, sourceConnector);
                        }
                    }

                    index++;
                }

                // add a result block to the output of the model block
                Block postBlock = this.createBlock(this.sinkBlock,
                    3,
                    onlineBlock.getX(),
                    (onlineBlock.getY() + 120));
                online.addBlock(postBlock);
                online.addWire(new Wire(
                    onlineBlock.getUniqueName(),
                    onlineBlock.getOutputConnectors().get(0).getName(),
                    0,
                    postBlock.getUniqueName(),
                    postBlock.getInputConnectors().get(0).getName(),
                    0));
            }
        }

        return online;
    }

    private Block createBlock(Definition definition, int state, int x, int y){

        // create name
        String name = "";
        int index = 1;
        do{
            name = definition.getName() + index;
            index++;
        }while(_onlineBlocks.contains(name));

        _onlineBlocks.add(name);

        return new Block(name, state, x, y, definition);
    }

    private void addLeadingPath(Wire wire, Diagram source, Diagram destination, boolean isSourceConnector){

        // todo: update to make copies

        String blockUniqueName = wire.getFrom_node();
        Block block = source.getBlockByUniqueName(blockUniqueName);

        if (isSourceConnector && block.getInputConnectors().size() == 0) {

            if (_dataSources.containsKey(block.getName())) {
                block = destination.getBlock(_dataSources.get(block.getName()));
            }
            else {
                String name = block.getName();
                int state = block.getState();
                int x = block.getX();
                int y = block.getY();
                block = createBlock(this.sourceBlock, state, x, y);
                destination.addBlock(block);

                _dataSources.put(name, block.getName());
            }

            wire.setFrom_node(block.getUniqueName());
        }

        if (!_onlineBlocks.contains(block.getName())){

            destination.addBlock(block);
            _onlineBlocks.add(block.getName());
        }

        destination.addWire(wire);

        for (Wire leadingWire : source.getLeadingWires(blockUniqueName))
            this.addLeadingPath(leadingWire, source, destination, isSourceConnector);
    }
}
