package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;


import java.util.ArrayList;
import java.util.List;

public class Model extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition(DefinitionType.MODEL, "Model", "Model", Category.TRANSFORMERS.toString());
        definition.setW(200);
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
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("model", DataType.LIST.toString(), true, true));
        return outputConnectors;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        List<ConnectorDefinition> inputs = new ArrayList<ConnectorDefinition>();
        inputs.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        modeDefinition.setInputs(inputs);

        List<ConnectorDefinition> outputs = new ArrayList<ConnectorDefinition>();
        outputs.add(new ConnectorDefinition("out", DataType.FLOAT.toString()));
        modeDefinition.setOutputs(outputs);

        modeDefinition.setSignature(new Signature("emr.analytics.spark.algorithms.Utilities",
                "Utilities",
                "predict",
                new String[]{
                        "input:in",
                        "block:model"
                }));

        return modeDefinition;
    }
}
