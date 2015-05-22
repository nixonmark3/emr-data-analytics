package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;


import java.util.ArrayList;
import java.util.List;

public class PLS extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("PLS", "PLS Analysis", Category.TRANSFORMERS.toString());
        definition.setOnlineComplement("Predict");
        return definition;
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("y", DataType.FRAME.toString()));
        return inputConnectors;
    }

    @Override
    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("model", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("ycomp", DataType.FRAME.toString()));
        return outputConnectors;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

        return null;
    }

    @Override
    public Signature createSignature() {

        return null;
    }

}
