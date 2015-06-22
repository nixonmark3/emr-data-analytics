package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class DataBrick extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("DataBrick", "Data Brick", Category.DATA_SOURCES.toString());
        definition.setDescription("Loads a data brick");
        return definition;
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());

        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Project",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Bricks",
                        new ArrayList<Argument>())));

        parameters.add(new ParameterDefinition("Query",
                DataType.QUERY.toString(),
                "None",
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
