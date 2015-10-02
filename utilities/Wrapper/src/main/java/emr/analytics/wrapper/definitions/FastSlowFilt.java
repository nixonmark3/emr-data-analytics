package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class FastSlowFilt extends BlockDefinition  implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("FastSlowFilt", "FastSlowFilt", Category.FILTERS.toString());
        definition.setDescription("1st order filter with different constants for increasing and decreasing signals");
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

        parameters.add(new ParameterDefinition("Fast_Filter",
                DataType.FLOAT.toString().toString(),
                .8,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Slow_Filter",
                DataType.FLOAT.toString().toString(),
                .2,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("AddSuffix",
                DataType.STRING.toString().toString(),
                "_FSF",
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
