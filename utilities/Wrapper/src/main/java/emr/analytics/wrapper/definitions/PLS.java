package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;


import java.util.ArrayList;
import java.util.List;

public class PLS extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition(DefinitionType.MODEL, "PLS", "PLS Model", Category.TRANSFORMERS.toString());
        definition.setMode(Mode.OFFLINE);
        definition.setComplement("PLS Predict");
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
        return null;
    }
}
