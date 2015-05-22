package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class SavGolay extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("SavGolay", "Savitsky-Golay Filter", Category.FILTERS.toString());
        definition.setDescription("Apply Savitsky-Golay filter to a given data frame");
        return definition;
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        return inputConnectors;
    }

    @Override
    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        return outputConnectors;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

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
        return parameters;
    }

    @Override
    public Signature createSignature() {

        return null;
    }

}
