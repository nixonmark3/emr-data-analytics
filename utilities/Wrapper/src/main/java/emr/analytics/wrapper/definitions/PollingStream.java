package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class PollingStream implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition;

        definition = new Definition("PollingStream", "Polling Stream", Category.DATA_SOURCES.toString());
        definition.setDescription("Spark streaming block that polls a specified url.");
        definition.setOnlineOnly(true);
        definition.setSignature(new Signature("emr.analytics.spark.algorithms.Sources",
                        "Sources",
                        "PollingStream",
                        new String[]{
                                "ssc",
                                "parameter:Url",
                                "parameter:Sleep"
                        })
        );

        // add output connector
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

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

        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
