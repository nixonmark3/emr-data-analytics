package services;

import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DiagramsService {

    private HashSet<String> _onlineBlocks;
    private HashMap<String, String> _dataSources;

    public Diagram compile(Diagram offline){

        Diagram online = new Diagram(offline.getName(),
            offline.getDescription(),
            offline.getOwner());

        HashMap<String, Definition> onlineDefinitions = getOnlineDefinitions();

        _onlineBlocks = new HashSet<>();
        _dataSources = new HashMap<>();

        // find model block - currently model blocks will exist in the collection of online definitions
        // todo: establish a more definitive definition of a model block
        for(Block block : offline.getBlocks()){

            if (onlineDefinitions.containsKey(block.getDefinition())){

                // reference online definition
                Definition onlineDefinition = onlineDefinitions.get(block.getDefinition());

                // create online block
                Block onlineBlock = this.createBlock(onlineDefinition,
                        block.getState(),
                        block.getX(),
                        block.getY());
                // set model parameter
                //todo: clean this up
                onlineBlock.setParameter("Model", block.getName());

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
                for(Connector connector : onlineBlock.getInputConnectors()){

                    if (block.hasInputConnector(connector.getName())){

                        for(Wire wire : offline.getLeadingWires(block.getName(), connector.getName())){

                            Wire onlineWire = new Wire(wire.getFrom_node(),
                                wire.getFrom_connector(),
                                wire.getFrom_connectorIndex(),
                                onlineBlock.getName(),
                                connector.getName(),
                                index);

                            this.addLeadingPath(onlineWire, offline, online);
                        }
                    }

                    index++;
                }

                // add a result block to the output of the model block
                Block postBlock = createBlock(this.getWebServicePostBlockDefinition(),
                    3,
                    onlineBlock.getX(),
                    (onlineBlock.getY() + 120));
                online.addBlock(postBlock);
                online.addWire(new Wire(
                    onlineBlock.getName(),
                    onlineBlock.getOutputConnectors().get(0).getName(),
                    0,
                    postBlock.getName(),
                    onlineBlock.getInputConnectors().get(0).getName(),
                    0));
            }
        }

        return online;
    }

    /**
     * Loads online definitions organized by the name of the offline definition counterpart
     */
    private HashMap<String, Definition> getOnlineDefinitions(){

        // todo: online definitions are currently hardcoded
        // todo: this should be updated so that these are created in the utilities wrapper project and loaded here from mongo

        HashMap<String, Definition> definitions = new HashMap<>();
        Definition definition;

        // create sensitivity definition
        definition = new Definition("PLS", "PLS Predict", Category.TRANSFORMERS.toString());
        definition.setDescription("Uses PLS model for prediction.");

        // add input connector
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        // add output connector
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FLOAT.toString()));
        definition.setOutputConnectors(outputConnectors);

        // add parameters
        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Model",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));
        definition.setParameters(parameters);

        definitions.put("Sensitivity", definition);

        return definitions;
    }

    private Definition getKafkaBlockDefinition(){

        Definition definition;

        // create sensitivity definition
        definition = new Definition("Kafka", "Kafka Data", Category.DATA_SOURCES.toString());
        definition.setDescription("Spark streaming block that monitors a topic in Kafka.");

        // add output connector
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        // todo: add parameters for zookeeper, kafka, topic name, and frequency

        return definition;
    }

    private Definition getWebServicePostBlockDefinition(){

        Definition definition;

        // create sensitivity definition
        definition = new Definition("RESTPost", "REST POST", Category.TRANSFORMERS.toString());
        definition.setDescription("Post data to a REST API.");

        // add input connector
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.STRING.toString()));
        definition.setInputConnectors(inputConnectors);

        // todo: add parameters for url, payload pattern

        return definition;
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

    private void addLeadingPath(Wire wire, Diagram source, Diagram destination){

        // todo: update to make copies

        String blockName = wire.getFrom_node();
        Block block = source.getBlock(blockName);

        // todo: temporarily replace offline databrick with online kafka block
        if (block.getDefinition().equals("DataBrick")) {

            if (_dataSources.containsKey(block.getName())) {
                block = destination.getBlock(_dataSources.get(block.getName()));
            }
            else {
                String name = block.getName();
                int state = block.getState();
                int x = block.getX();
                int y = block.getY();
                block = createBlock(this.getKafkaBlockDefinition(), state, x, y);
                destination.addBlock(block);

                _dataSources.put(name, block.getName());
            }

            wire.setFrom_node(block.getName());
        }

        if (!_onlineBlocks.contains(block.getName())){

            destination.addBlock(block);
            _onlineBlocks.add(block.getName());
        }

        destination.addWire(wire);

        for (Wire leadingWire : source.getLeadingWires(blockName))
            this.addLeadingPath(leadingWire, source, destination);
    }
}
