package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class Kafka extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition;

        definition = new Definition("Kafka", "Kafka Data", Category.DATA_SOURCES.toString());
        definition.setDescription("Spark streaming block that monitors a topic in Kafka.");
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

        parameters.add(new ParameterDefinition("Zookeeper Quorum",
                DataType.STRING.toString(),
                "localhost:2181",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Topics",
                DataType.STRING.toString(),
                "runtime",
                new ArrayList<String>(),
                null));

        return parameters;
    }

    @Override
    public Signature createSignature() {

        return new Signature("emr.analytics.spark.algorithms.Utilities",
                "Utilities",
                "kafkaStream",
                new String[]{
                        "ssc",
                        "parameter:Zookeeper Quorum",
                        "appName",
                        "parameter:Topics"
                });
    }

}
