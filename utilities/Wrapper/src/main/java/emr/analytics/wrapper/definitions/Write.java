package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class Write extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition;

        definition = new Definition("Write", "Write", Category.TRANSFORMERS.toString());
        definition.setDescription("Write data base to a location(s).");

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
        inputConnectors.add(new ConnectorDefinition("in", DataType.STRING.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        return null;
    }

    public List<ParameterDefinition> createParameters() {

        String defaultValue = "{\n"
                + "  \"consumers\" : [\n"
                + "    {\n"
                + "      \"consumerType\": \"Simulated\",\n"
                + "      \"ip\": \"localhost\",\n"
                + "      \"tag\": \"TAG1\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"consumerType\": \"OPC\",\n"
                + "      \"ip\": \"000.000.00.000\",\n"
                + "      \"tag\": \"TAG1\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"consumerType\": \"PI\",\n"
                + "      \"ip\": \"000.000.00.000\",\n"
                + "      \"tag\": \"TAG1\"\n"
                + "    }\n"
                + "  ]\n"
                + "}\n";

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Query",
                DataType.EDITABLE_QUERY.toString(),
                defaultValue,
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
