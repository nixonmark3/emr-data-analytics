package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class PCA_NIPALS_TEST extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {
        Definition definition = new Definition("PCA_NIPALS_TEST", "PCA_NIPALS_TEST", Category.TRANSFORMERS.toString());
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
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("Loadings", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("EigenValues", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("EigenVectors", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("Mean", DataType.FLOAT.toString()));
        inputConnectors.add(new ConnectorDefinition("Std", DataType.FLOAT.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("Loadings", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("Scores", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("ScoresCont", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("T2", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("T2Lim", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("T2Cont", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("Q", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("QLim", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("QCont", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("EigenValues", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("EigenVectors", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("Mean", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("Std", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Confidence Level",
                DataType.FLOAT.toString(),
                0.95,
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
