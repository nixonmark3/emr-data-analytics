package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class PLSSensitivity extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("Sensitivity", "PLSSensitivity", Category.TRANSFORMERS.toString());
        definition.setDescription("Calculates sensitivity of output y to set of inputs X using PLS");
        return definition;
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setInputs(createInputConnectors());
        modeDefinition.setOutputs(createOutputConnectors());

        return modeDefinition;
    }

    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("y", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("model", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("coefs", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("r2", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("ycomp", DataType.FRAME.toString()));
        return outputConnectors;
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
                                "block:model"
                        }),
                new Operation(Operation.OperationType.MAP,
                        "Transformations",
                        "normalize",
                        new String[]{
                                "lambda:x",
                                "block:y_mean",
                                "block:y_std"
                        })
        }));

        return modeDefinition;
    }
}
