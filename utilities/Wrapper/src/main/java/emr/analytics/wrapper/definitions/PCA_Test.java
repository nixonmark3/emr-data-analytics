package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class PCA_Test extends BlockDefinition  implements IExport {

    @Override
    public Definition createDefinition() {

        return new Definition("PCA_Test", "PCA Test", Category.TRANSFORMERS.toString());
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setInputs(createInputConnectors());
        modeDefinition.setOutputs(createOutputConnectors());

        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("data", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("Loadings", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("OrigMean", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("OrigSTD", DataType.FRAME.toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("Scores", DataType.FRAME.toString()));
        return outputConnectors;
    }
}
