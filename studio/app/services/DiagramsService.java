package services;

import emr.analytics.models.definition.Category;
import emr.analytics.models.definition.ConnectorDefinition;
import emr.analytics.models.definition.DataType;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DiagramsService {

    public Diagram compile(Diagram offline){

        Diagram online = new Diagram(offline.getName(),
            offline.getDescription(),
            offline.getOwner());

        HashSet<String> onlineBlocks = new HashSet<>();
        HashMap<String, Definition> onlineDefinitions = getOnlineDefinitions();

        // find model block - currently model blocks will exist in the collection of online definitions
        // todo: establish a more definitive definition of a model block
        for(Block block : offline.getBlocks()){

            if (onlineDefinitions.containsKey(block.getDefinition())){

                // reference online definition
                Definition onlineDefinition = onlineDefinitions.get(block.getDefinition());

                // create name
                String name = "";
                int index = 1;
                do{
                    name = onlineDefinition.getName() + index;
                    index++;
                }while(onlineBlocks.contains(name));

                // create online block
                Block onlineBlock = new Block(name,
                        block.getState(),
                        block.getX(),
                        block.getY(),
                        onlineDefinition);
                // set collected parameter values
                for(Parameter parameter : block.getParameters()){

                    if (parameter.isCollected())
                        onlineBlock.setParameter(parameter.getName(), parameter.getValue());
                }

                // add online block to diagram
                online.addBlock(onlineBlock);
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
        definition = new Definition("PLSModel", "PLS Model", Category.TRANSFORMERS.toString());
        definition.setDescription("Uses PLS model for prediction.");

        // add input connector
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        // add output connector
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FLOAT.toString()));
        definition.setOutputConnectors(outputConnectors);

        definitions.put("Sensitivity", definition);

        return definitions;
    }


}
