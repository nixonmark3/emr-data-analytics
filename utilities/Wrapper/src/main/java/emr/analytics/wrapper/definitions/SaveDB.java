package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class SaveDB extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("SaveDB", "Save DB", Category.DATA_SOURCES.toString());
        definition.setDescription("Saves a given data frame");
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
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Brick",
                ParameterType.ENUMERATION,
                ValueType.SCALAR,
                "",
                new ArrayList<String>(),
                new ParameterSource(ParameterSource.ParameterSourceTypes.JAR,
                        "plugins-1.0-SNAPSHOT.jar",
                        "Bricks",
                        new ArrayList<Argument>())));

        parameters.add(new ParameterDefinition("New Brick Name",
                ParameterType.STRING,
                ValueType.SCALAR,
                "",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Data Set Name",
                ParameterType.STRING,
                ValueType.SCALAR,
                "None",
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
