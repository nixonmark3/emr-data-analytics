package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;
import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class ExponentialFilter implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("ExpFilter", "Exp Filter", Category.FILTERS.toString());

        definition.setDescription("Apply exponential filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Alpha",
                DataType.FLOAT.toString(),
                0.8,
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Order",
                DataType.INT.toString().toString(),
                1,
                new ArrayList<String>(),
                null));
        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
