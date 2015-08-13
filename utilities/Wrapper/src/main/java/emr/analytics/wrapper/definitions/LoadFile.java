package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class LoadFile extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("LoadFile", "Load File", Category.DATA_SOURCES.toString());
        definition.setDescription("Loads a data set from a file");
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

        parameters.add(new ParameterDefinition("Filename",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        List<String> opts = new ArrayList<String>();
        opts.add("True");
        opts.add("False");

        parameters.add(new ParameterDefinition("Time Series",
                DataType.LIST.toString(),
                "True",
                opts,
                null));

        parameters.add(new ParameterDefinition("Plot",
                DataType.LIST.toString(),
                "False",
                opts,
                null));

        List<String> fileType = new ArrayList<String>();
        fileType.add("CSV");
        fileType.add("FF3");
        fileType.add("CDA");

        parameters.add(new ParameterDefinition("File Type",
                DataType.LIST.toString(),
                "CSV",
                fileType,
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
