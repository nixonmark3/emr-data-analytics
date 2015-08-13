package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class LoadDB extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("LoadDB", "Load DB", Category.DATA_SOURCES.toString());
        definition.setDescription("Loads a data set from a given project");
        return definition;
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());

        return modeDefinition;
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

        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new Argument("Project", 0, "Project.Value"));

        parameters.add(new ParameterDefinition("Data Set",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "DataSets",
                        arguments)));

        List<String> opts = new ArrayList<String>();
        opts.add("True");
        opts.add("False");

        parameters.add(new ParameterDefinition("Plot",
                DataType.LIST.toString(),
                "False",
                opts,
                null));

        return parameters;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();

        // create outputs
        List<ConnectorDefinition> outputs = new ArrayList<ConnectorDefinition>();
        outputs.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        modeDefinition.setOutputs(outputs);

        // create parameters
        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Topic",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));
        modeDefinition.setParameters(parameters);

        modeDefinition.setSignature(new Signature("StreamingSources",
                "kafkaStream",
                new String[]{
                        "ssc",
                        "parameter:Topic",
                        "broker"
                }));

        return modeDefinition;
    }
}
