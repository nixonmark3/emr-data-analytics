package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class SavGolay implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("SavGolay", "Savitsky-Golay Filter", Category.FILTERS.toString());

        definition.setDescription("Apply Savitsky-Golay filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("PointsToLeft",
                DataType.INT.toString().toString(),
                10,
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("PointsToRight",
                DataType.INT.toString().toString(),
                10,
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("PolynomialOrder",
                DataType.INT.toString().toString(),
                3, new ArrayList<String>(),
                null));
        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
