/*
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
        modeDefinition.setSignature(createSignature());
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
        outputConnectors.add(new ConnectorDefinition("out", DataType.LIST.toString(), true, false));
        return outputConnectors;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

    public Signature createSignature() {

        return new Signature("emr.analytics.spark.algorithms.Utilities",
                "Utilities",
                "fillNa",
                new String[]{
                        "input:in"
                });
    }
}
*/
