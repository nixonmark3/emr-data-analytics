package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class RadPlot extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {
        Definition definition = new Definition("RadPlot", "RadPlot", Category.TRANSFORMERS.toString());
        definition.setW(300);
        return definition;
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setInputs(createInputConnectors());
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());

        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

    public List<ConnectorDefinition> createInputConnectors() {
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("Data", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("Mean", DataType.FLOAT.toString()));
        inputConnectors.add(new ConnectorDefinition("Std", DataType.FLOAT.toString()));
        inputConnectors.add(new ConnectorDefinition("Loadings", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("Eigenvalues", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Autopick(1 = yes, 0 = no)",
                DataType.INT.toString(),
                0,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Moving Window Size",
                DataType.INT.toString(),
                30,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Start",
                DataType.INT.toString(),
                0,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("End",
                DataType.INT.toString(),
                200,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("VarCapNumPC",
                DataType.FLOAT.toString(),
                0.7,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("ConfInt",
                DataType.FLOAT.toString(),
                0.95,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("NumContVar",
                DataType.INT.toString(),
                2,
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("OutputIndex",
                DataType.INT.toString(),
                1,
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
