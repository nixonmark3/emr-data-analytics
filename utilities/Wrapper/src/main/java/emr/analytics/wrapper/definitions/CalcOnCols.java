package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class CalcOnCols extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        return new Definition("RowDelete", "Delete Rows", Category.FILTERS.toString());
    }

    @Override
    public ModeDefinition createOfflineMode() {

        ModeDefinition modeDefinition = new ModeDefinition();
        //modeDefinition.setSignature(createSignature());
        modeDefinition.setInputs(createInputConnectors());
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());
        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode() {

        return null;
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
        parameters.add(new ParameterDefinition("Query",
                DataType.QUERY.toString(),
                "None",
                new ArrayList<String>(),
                null));
        return parameters;
    }
}
