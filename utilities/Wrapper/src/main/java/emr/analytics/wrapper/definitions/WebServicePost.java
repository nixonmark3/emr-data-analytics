package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class WebServicePost implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition;

        definition = new Definition("RESTPost", "REST POST", Category.TRANSFORMERS.toString());
        definition.setDescription("Post data to a REST API.");
        definition.setOnlineOnly(true);
        definition.setSignature(new Signature("emr.analytics.spark.algorithms.Requests",
                        "Requests",
                        "postOpcValue",
                        new String[]{
                                "parameter:Url",
                                "parameter:Tag",
                                "input:in"
                        })
        );

        // add input connector
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.STRING.toString()));
        definition.setInputConnectors(inputConnectors);

        // todo: add parameters for url, payload pattern
        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Url",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Tag",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));

        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
