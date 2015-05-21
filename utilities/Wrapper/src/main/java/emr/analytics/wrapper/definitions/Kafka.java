package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class Kafka implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition;

        definition = new Definition("Kafka", "Kafka Data", Category.DATA_SOURCES.toString());
        definition.setDescription("Spark streaming block that monitors a topic in Kafka.");
        definition.setOnlineOnly(true);
        definition.setSignature(new Signature("emr.analytics.spark.algorithms.Utilities",
                        "Utilities",
                        "kafkaStream",
                        new String[]{
                                "ssc",
                                "parameter:Zookeeper Quorum",
                                "appName",
                                "parameter:Topics"
                        })
        );

        // add output connector
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

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

        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
