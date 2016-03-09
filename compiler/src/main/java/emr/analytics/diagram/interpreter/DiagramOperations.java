package emr.analytics.diagram.interpreter;

import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.DefinitionType;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.*;
import emr.analytics.models.messages.Consumers;

import java.util.*;

public class DiagramOperations {

    public static Diagram group(String name, Diagram parent, String[] blockIds){

        // create a set of block ids for reference
        Set<String> blockIdSet = new HashSet<String>(Arrays.asList(blockIds));
        // create a new diagram
        Diagram diagram = new Diagram(name);

        // create variables to track the group's collective minimum position position
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, xOrigin = 100, yOrigin = 50;

        // and a complimentary block

        Block block = new Block(name,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Mode.OFFLINE,
                new Definition(DefinitionType.CONTAINER,
                        name,
                        name,
                        "Block Group"));

        parent.addBlock(block);

        // iterate over each block
        for(String blockIdString : blockIds){

            UUID blockId = UUID.fromString(blockIdString);

            // find the specified block
            Block groupBlock = parent.getBlock(blockId);

            // remove it from the diagram
            parent.removeBlock(groupBlock);

            // add it to the block group
            diagram.addBlock(groupBlock);

            // track the group block's origin
            if (groupBlock.getX() < x)
                x = groupBlock.getX();
            if (groupBlock.getY() < y)
                y = groupBlock.getY();

            // capture the group block's dimensions that are closest to the top
            if (groupBlock.getY() < block.getY()){
                block.setX(groupBlock.getX());
                block.setY(groupBlock.getY());
            }

            // capture the group block's state that is lowest
            /*if (groupBlock.getState() < block.getState())
                block.setState(groupBlock.getState());*/

            // iterate over each input connector - creating group connectors
            for(Connector connector : groupBlock.getInputConnectors()){

                List<WireSummary> wires = parent.getInputWires(blockId, connector.getName());

                Connector groupConnector;;
                if (wires.size() == 0){
                    groupConnector = new Connector(connector.getName(), connector.getType());
                    block.addInputConnector(groupConnector);
                }

                boolean isGroupConnector = false;
                for(int i = 0; i < wires.size(); i++){

                    Wire wire = wires.get(i);

                    if (!blockIdSet.contains(wire.getFrom_node())) {

                        // flag as a group connector
                        isGroupConnector = true;

                        // get or create the specified block connector
                        Optional<Connector> optional = block.getInputConnector(connector.getName());
                        if (optional.isPresent()) {
                            groupConnector = optional.get();
                        }
                        else{
                            groupConnector = new Connector(connector.getName(), connector.getType());
                            block.addInputConnector(groupConnector);
                        }

                        // create a wire from this wire's source to our new block
                        parent.addWire(new Wire(wire.getFrom_node(),
                                wire.getFrom_connector(),
                                wire.getFrom_connectorIndex(),
                                block.getId(),
                                groupConnector.getName(),
                                0));  // todo: invalid connector index -> do we use connector index

                        // remove this wire from the diagram
                        parent.removeWire(wire);
                    }
                }

                if (isGroupConnector){
                    diagram.addInput(new DiagramConnector(blockId,
                            String.format("%s_%s", groupBlock.getName(), connector.getName()),
                            connector.getType()));
                }
            }

            // iterate over each output connector - consuming wires and creating group connectors
            for(Connector connector : groupBlock.getOutputConnectors()){

                boolean isGroupConnector = false;
                List<WireSummary> wires = parent.getOutputWires(blockId, connector.getName());

                Connector groupConnector;;
                if (wires.size() == 0){
                    groupConnector = new Connector(connector.getName(), connector.getType());
                    block.addOutputConnector(groupConnector);
                }

                for(int i = 0; i < wires.size(); i++){

                    Wire wire = wires.get(i);

                    if (!blockIdSet.contains(wire.getTo_node())) {
                        // this wire does not connect to a block destined for this group -
                        // therefore it is a group connector
                        isGroupConnector = true;

                        // get or create the specified block connector
                        Optional<Connector> optional = block.getOutputConnector(connector.getName());
                        if (optional.isPresent()) {
                            groupConnector = optional.get();
                        }
                        else{
                            groupConnector = new Connector(connector.getName(), connector.getType());
                            block.addOutputConnector(groupConnector);
                        }

                        // create a wire from our new block to this wires destination
                        parent.addWire(new Wire(block.getId(),
                                groupConnector.getName(),
                                0,                          // todo: invalid connector index -> do we use connector index
                                wire.getTo_node(),
                                wire.getTo_connector(),
                                wire.getTo_connectorIndex()));

                        // remove this wire from the diagram
                        parent.removeWire(wire);
                    }
                    else{
                        // leads to a block destined for this group - consume wire
                        diagram.addWire(wire);
                    }
                }

                if (isGroupConnector){
                    diagram.addOutput(new DiagramConnector(blockId,
                            String.format("%s_%s", groupBlock.getName(), connector.getName()),
                            connector.getType()));
                }
            }
        }

        // calculate group origin delta and adjust each group block's position
        int xDelta = x - xOrigin;
        int yDelta = y - yOrigin;
        for(Block groupBlock : diagram.getBlocks()){

            groupBlock.setX(groupBlock.getX() - xDelta);
            groupBlock.setY(groupBlock.getY() - yDelta);
        }

        // remove group wires from the diagram
        for(Wire wire : diagram.getWires())
            parent.removeWire(wire);

        // add this new group to the diagram
        parent.addDiagram(diagram);

        return parent;
    }

    public static Consumers getOnlineConsumers(Diagram diagram){

        Consumers consumers = null;

        // search for the diagrams output block (there can be only one)
        Optional<Block> temp = diagram.getBlocks().stream().filter(b -> b.getDefinitionType().equals(DefinitionType.OUTPUT)).findFirst();
        if (temp.isPresent()){
            Block block = temp.get();

            ObjectMapper mapper = new ObjectMapper();
            consumers = mapper.convertValue(block.getParameters().get(0).getValue(), Consumers.class);
        }
        return consumers;
    }
}
