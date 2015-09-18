package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class QuadDiscA extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition(DefinitionType.MODEL, "LinDiscA", "Linear Discriminant Analysis", Category.TRANSFORMERS.toString());
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
        inputConnectors.add(new ConnectorDefinition("test_x", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("test_y", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("y_train_comp", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("y_test_comp", DataType.FRAME.toString()));
        return outputConnectors;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }




}
