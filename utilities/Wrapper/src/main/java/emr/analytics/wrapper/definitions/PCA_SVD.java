package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class PCA_SVD extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {
        return new Definition("PCA_SVD", "PCA_SVD", Category.TRANSFORMERS.toString());
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        return inputConnectors;
    }

    @Override
    public List<ConnectorDefinition> createOutputConnectors() {
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("scores", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("loadings", DataType.FRAME.toString()));
        return outputConnectors;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("N Components",
                DataType.INT.toString(),
                2,
                new ArrayList<String>(),
                null));

        return parameters;
    }

    @Override
    public Signature createSignature() {
        return null;
    }
}