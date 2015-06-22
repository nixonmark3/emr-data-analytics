package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class Columns extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("Columns", "Columns", Category.TRANSFORMERS.toString());
        definition.setDescription("Selects columns from a given data frame");
        return definition;
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setSignature(createSignature());
        modeDefinition.setInputs(createInputConnectors());
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());

        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

    public Signature createSignature() {

        return new Signature("emr.analytics.spark.algorithms.Utilities",
                        "Utilities",
                        "columns",
                        new String[]{
                                "input:in",
                                "parameter:Columns"
                        });
    }

    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

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

        return parameters;
    }

}
