package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class DownSample extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("DownSample", "Down Sample", Category.TRANSFORMERS.toString());
        definition.setDescription("Down sample a given data frame");
        return definition;
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setInputs(createInputConnectors());
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());

        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("SampleSize",
                DataType.INT.toString(),
                100,
                new ArrayList<String>(),
                null));

        List<String> opts = new ArrayList<String>();
        opts.add("First");
        opts.add("Last");
        opts.add("Mean");

        parameters.add(new ParameterDefinition("Interpolation",
                DataType.LIST.toString(),
                "Last",
                opts,
                null));

        return parameters;
    }
}
