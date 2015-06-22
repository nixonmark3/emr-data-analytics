package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class CreateDB extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        return new Definition("CreateDB", "Create DB", Category.DATA_SOURCES.toString());
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setParameters(createParameters());

        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

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
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Data Set Name",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
