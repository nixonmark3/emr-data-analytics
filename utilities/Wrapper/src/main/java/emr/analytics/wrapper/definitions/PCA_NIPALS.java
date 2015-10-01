package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class PCA_NIPALS extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {
        Definition definition = new Definition(DefinitionType.MODEL, "PCA_NIPALS", "PCA_NIPALS", Category.TRANSFORMERS.toString());
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

    public List<ConnectorDefinition> createInputConnectors() {
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("Loadings", DataType.FRAME.toString(), true, true));
        outputConnectors.add(new ConnectorDefinition("Scores", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("ScoresCont", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("T2", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("T2Lim", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("T2Cont", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("Q", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("QLim", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("QCont", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("EigenValues", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("EigenVectors", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("Mean", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("Std", DataType.FRAME.toString(), true, false));
        outputConnectors.add(new ConnectorDefinition("x_mean", DataType.LIST.toString(), false, true));
        outputConnectors.add(new ConnectorDefinition("x_std", DataType.LIST.toString(), false, true));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("N Components",
                DataType.INT.toString(),
                2,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Confidence Level",
                DataType.FLOAT.toString(),
                0.95,
                new ArrayList<String>(),
                null));

        return parameters;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        List<ConnectorDefinition> inputs = new ArrayList<ConnectorDefinition>();
        inputs.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        modeDefinition.setInputs(inputs);

        List<ConnectorDefinition> outputs = new ArrayList<ConnectorDefinition>();
        outputs.add(new ConnectorDefinition("out", DataType.FLOAT.toString()));
        modeDefinition.setOutputs(outputs);

        modeDefinition.setSignature(new Signature("input:x", new Operation[]{
                new Operation(Operation.OperationType.MAP,
                        "Transformations",
                        "normalize",
                        new String[]{
                                "lambda:x",
                                "block:x_mean",
                                "block:x_std"
                        }),
                new Operation(Operation.OperationType.MAP,
                        "Transformations",
                        "dotProduct",
                        new String[]{
                                "lambda:x",
                                "block:Loadings"
                        })
        }));

        return modeDefinition;
    }

}
