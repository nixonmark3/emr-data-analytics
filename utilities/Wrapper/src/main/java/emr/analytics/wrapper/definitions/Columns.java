package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class Columns implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("Columns", "Columns", Category.TRANSFORMERS.toString());

        definition.setDescription("Selects columns from a given data frame");

        definition.setSignature(new Signature("emr.analytics.spark.algorithms.Utilities",
                        "Utilities",
                        "columns",
                        new String[]{
                                "input:in",
                                "parameter:Columns"
                        })
        );

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new Argument("BlockName", 1, "BlockName.Value"));

        parameters.add(new ParameterDefinition("Columns",
                DataType.MULTI_SELECT_LIST.toString(),
                new ArrayList<String>(),
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Columns",
                        arguments)));

        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
