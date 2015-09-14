package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class AutoPickTrain extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {
        Definition definition = new Definition("AutoPickTrain", "AutoPickTrain", Category.TRANSFORMERS.toString());
        definition.setW(300);
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
        inputConnectors.add(new ConnectorDefinition("Data", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Autopick(1 = yes, 0 = no)",
                DataType.INT.toString(),
                0,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Moving Window Size",
                DataType.INT.toString(),
                30,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Start",
                DataType.INT.toString(),
                0,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("End",
                DataType.INT.toString(),
                200,
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
