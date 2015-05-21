package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class SaveCSV implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("SaveCSV", "Save CSV", Category.DATA_SOURCES.toString());

        definition.setDescription("Saves a given data frame to CSV");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Filename",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
