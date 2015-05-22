package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class PollingStream extends BlockDefinition  implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition;

        definition = new Definition("PollingStream", "Polling Stream", Category.DATA_SOURCES.toString());
        definition.setDescription("Spark streaming block that polls a specified url.");
        definition.setOnlineOnly(true);

        return definition;
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {

        return null;
    }

    @Override
    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

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

        return parameters;
    }

    @Override
    public Signature createSignature() {

        return new Signature("emr.analytics.spark.algorithms.Sources",
                "Sources",
                "PollingStream",
                new String[]{
                        "ssc",
                        "parameter:Url",
                        "parameter:Sleep"
                });
    }

}
