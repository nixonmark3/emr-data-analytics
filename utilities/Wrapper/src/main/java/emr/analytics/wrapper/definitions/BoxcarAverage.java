package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class BoxcarAverage extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("BoxcarAve", "Boxcar Ave", Category.FILTERS.toString());
        definition.setDescription("Apply Boxcar average filter to a given data frame");
        return definition;
    }

    @Override
    public Signature createSignature() {

        return null;
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
        parameters.add(new ParameterDefinition("WindowSize",
                DataType.INT.toString().toString(),
                20,
                new ArrayList<String>(),
                null));
        return parameters;
    }

}
