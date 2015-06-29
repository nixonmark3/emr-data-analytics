package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class LoadSeeq extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {
        return new Definition("LoadSeeq", "Load Seeq", Category.DATA_SOURCES.toString());
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());

        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode() {

        ModeDefinition modeDefinition = new ModeDefinition();

        List<ConnectorDefinition> outputs = new ArrayList<ConnectorDefinition>();
        outputs.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        modeDefinition.setOutputs(outputs);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Url",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Sleep",
                DataType.STRING.toString(),
                "500",
                new ArrayList<String>(),
                null));

        modeDefinition.setParameters(parameters);

        Signature signature = new Signature("emr.analytics.spark.algorithms.Sources",
                "Sources",
                "PollingStream",
                new String[] {
                        "ssc",
                        "parameter:Url",
                        "parameter:Sleep"
                });

        modeDefinition.setSignature(signature);

        return modeDefinition;
    }


    public List<ConnectorDefinition> createOutputConnectors() {
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Seeq IP",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Capsule Name",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
