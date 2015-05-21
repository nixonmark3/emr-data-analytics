package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class RollingStdDev implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("RollingStdDev", "Rolling Std Dev", Category.FILTERS.toString());

        definition.setDescription("Determines the rolling deviation of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("WindowSize",
                DataType.INT.toString(),
                60,
                new ArrayList<String>(),
                null));
        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
