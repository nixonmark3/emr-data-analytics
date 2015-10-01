package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class Kmeans extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition(DefinitionType.MODEL, "Kmeans", "Kmeans cluster Analysis", Category.TRANSFORMERS.toString());
        definition.setW(200);
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
        outputConnectors.add(new ConnectorDefinition("labels", DataType.FRAME.toString()));
        return outputConnectors;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        List<String> opts = new ArrayList<String>();
        opts.add("True");
        opts.add("False");

        parameters.add(new ParameterDefinition("TimeStampResults",
                DataType.LIST.toString(),
                "True",
                opts,
                null));

        parameters.add(new ParameterDefinition("NumClusters",
                DataType.INT.toString(),
                8,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("NumRowsSample",
                DataType.INT.toString(),
                15,
                new ArrayList<String>(),
                null));

        return parameters;
    }


}
