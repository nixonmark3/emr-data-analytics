package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class AddDataSet extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        return new Definition("AddDataSet", "Add Data Set", Category.DATA_SOURCES.toString());
    }

    @Override
    public Signature createSignature() {

        return null;
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {

        return null;
    }


    @Override
    public List<ConnectorDefinition> createOutputConnectors() {

        return null;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

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

        return parameters;
    }
}
