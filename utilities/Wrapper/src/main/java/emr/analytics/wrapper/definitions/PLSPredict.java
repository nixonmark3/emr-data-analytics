package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;


import java.util.ArrayList;
import java.util.List;

public class PLSPredict extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition(DefinitionType.MODEL, "PLS Predict", "PLS Predict", Category.TRANSFORMERS.toString());
        definition.setMode(Mode.ONLINE);
        definition.setComplement("PLS");
        definition.setW(200);
        return definition;
    }

    @Override
    public ModeDefinition createOfflineMode(){
        return null;
    }

    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("y", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("model", DataType.LIST.toString(), true, true));
        outputConnectors.add(new ConnectorDefinition("x_mean", DataType.LIST.toString(), false, true));
        outputConnectors.add(new ConnectorDefinition("x_std", DataType.LIST.toString(), false, true));
        outputConnectors.add(new ConnectorDefinition("y_mean", DataType.LIST.toString(), false, true));
        outputConnectors.add(new ConnectorDefinition("y_std", DataType.LIST.toString(), false, true));
        outputConnectors.add(new ConnectorDefinition("y_comp", DataType.FRAME.toString()));
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

        modeDefinition.setSignature(new Signature("input:x", new Operation[] {
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
                        "deNormalize",
                        new String[]{
                                "lambda:x",
                                "block:y_mean",
                                "block:y_std"
                        })
        }));

        return modeDefinition;
    }
}
