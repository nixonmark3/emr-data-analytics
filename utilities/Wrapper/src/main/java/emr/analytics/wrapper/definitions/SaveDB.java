package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;
import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class SaveDB implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("SaveDB", "Save DB", Category.DATA_SOURCES.toString());

        definition.setDescription("Saves a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Project",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Projects",
                        new ArrayList<Argument>())));

        parameters.add(new ParameterDefinition("Data Set Name",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
