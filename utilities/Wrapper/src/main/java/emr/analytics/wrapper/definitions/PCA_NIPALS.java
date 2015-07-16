package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class PCA_NIPALS extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {
        return new Definition("PCA_NIPALS", "PCA_NIPALS", Category.TRANSFORMERS.toString());
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
        outputConnectors.add(new ConnectorDefinition("Scores", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("Loadings", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("Q", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("T2", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("N Components",
                DataType.INT.toString(),
                2,
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
