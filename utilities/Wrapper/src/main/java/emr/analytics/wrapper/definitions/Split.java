package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class Split implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = null;

        definition = new Definition("Split", "Split", Category.TRANSFORMERS.toString());
        definition.setDescription("Splits a data frame into training and testing data frames");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out1", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("out2", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Split",
                DataType.INT.toString(),
                75,
                new ArrayList<String>(),
                null));
        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
