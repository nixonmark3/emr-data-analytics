package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class ThreeSigma implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("ThreeSigma", "Three Sigma", Category.FILTERS.toString());

        definition.setDescription("Apply three (or g-order) sigma algorithm to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("MovingWindow",
                DataType.INT.toString().toString(),
                20,
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("g-Sigma",
                DataType.FLOAT.toString(),
                3.0,
                new ArrayList<String>(),
                null));
        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
