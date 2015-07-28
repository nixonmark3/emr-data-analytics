package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class LoadPi extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        return new Definition("LoadPi", "Load PI", Category.DATA_SOURCES.toString());
    }

    @Override
    public ModeDefinition createOfflineMode() {

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setSignature(createSignature());
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());
        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode() {

        return null;
    }

    public Signature createSignature() {

        return new Signature("emr.analytics.spark.algorithms.Utilities",
                "Utilities",
                "loadPi",
                new String[] {
                        "parameter:IP",
                        "parameter:Port",
                        "parameter:Query"
                });
    }


    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("IP",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Port",
                DataType.INT.toString(),
                "",
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Query",
                DataType.QUERY.toString(),
                "None",
                new ArrayList<String>(),
                null));
        return parameters;
    }
}
