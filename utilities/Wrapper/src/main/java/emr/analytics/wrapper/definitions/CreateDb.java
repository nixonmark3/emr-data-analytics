package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.Category;
import emr.analytics.models.definition.DataType;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.ParameterDefinition;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class CreateDB implements IExport{

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("CreateDB", "Create DB", Category.DATA_SOURCES.toString());

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Filename",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        List<String> opts = new ArrayList<String>();
        opts.add("CSV");
        opts.add("FF3");

        parameters.add(new ParameterDefinition("File Type",
                DataType.LIST.toString(),
                "CSV",
                opts,
                null));

        parameters.add(new ParameterDefinition("Project Name",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Data Set Name",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        definition.setParameters(parameters);

        definitions.save(definition);
    }
}
