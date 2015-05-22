package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class Split extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = null;
        definition = new Definition("Split", "Split", Category.TRANSFORMERS.toString());
        definition.setDescription("Splits a data frame into training and testing data frames");
        return definition;
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        return inputConnectors;
    }

    @Override
    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out1", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("out2", DataType.FRAME.toString()));
        return outputConnectors;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Split",
                DataType.INT.toString(),
                75,
                new ArrayList<String>(),
                null));
        return parameters;
    }

    @Override
    public Signature createSignature() {

        return null;
    }
}
