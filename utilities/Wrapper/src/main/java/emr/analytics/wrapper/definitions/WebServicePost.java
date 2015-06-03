package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class WebServicePost extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition;

        definition = new Definition("RESTPost", "REST POST", Category.TRANSFORMERS.toString());
        definition.setDescription("Post data to a REST API.");
        definition.setOnlineOnly(true);

        return definition;
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.STRING.toString()));
        return inputConnectors;
    }

    @Override
    public List<ConnectorDefinition> createOutputConnectors() {

        return null;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

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

        return parameters;
    }

    @Override
    public Signature createSignature() {

        return new Signature("emr.analytics.spark.algorithms.Requests",
                "Requests",
                "postOpcValue",
                new String[]{
                        "parameter:Url",
                        "parameter:Tag",
                        "input:in"
                });
    }

}