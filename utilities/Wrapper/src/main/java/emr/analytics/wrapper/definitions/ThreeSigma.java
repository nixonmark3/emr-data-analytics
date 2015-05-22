package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class ThreeSigma extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("ThreeSigma", "Three Sigma", Category.FILTERS.toString());
        definition.setDescription("Apply three (or g-order) sigma algorithm to a given data frame");
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
        return parameters;
    }

    @Override
    public Signature createSignature() {

        return null;
    }

}
