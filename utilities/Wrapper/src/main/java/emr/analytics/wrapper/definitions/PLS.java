package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.Category;
import emr.analytics.models.definition.ConnectorDefinition;
import emr.analytics.models.definition.DataType;
import emr.analytics.models.definition.Definition;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class PLS implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("PLS", "PLS Analysis", Category.TRANSFORMERS.toString());
        definition.setOnlineComplement("Predict");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("y", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("model", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("ycomp", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        definitions.save(definition);
    }
}
