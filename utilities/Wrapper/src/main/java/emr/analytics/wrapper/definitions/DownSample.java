package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class DownSample implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("DownSample", "Down Sample", Category.TRANSFORMERS.toString());

        definition.setDescription("Down sample a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("SampleSize",
                DataType.INT.toString(),
                100,
                new ArrayList<String>(),
                null));

        List<String> opts = new ArrayList<String>();
        opts.add("First");
        opts.add("Last");
        opts.add("Mean");

        parameters.add(new ParameterDefinition("Interpolation",
                DataType.LIST.toString(),
                "Last",
                opts,
                null));

        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
