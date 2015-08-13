package emr.analytics.models.definition;

import java.util.ArrayList;
import java.util.List;

public class ModeDefinition {

    private Signature signature = null;
    private List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
    private List<ConnectorDefinition> inputs = new ArrayList<ConnectorDefinition>();
    private List<ConnectorDefinition> outputs  = new ArrayList<ConnectorDefinition>();

    public Signature getSignature(){ return this.signature; }

    public void setSignature(Signature signature){ this.signature = signature; }

    public List<ParameterDefinition> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterDefinition> parameters) {
        this.parameters = parameters;
    }

    public List<ConnectorDefinition> getInputs() { return inputs; }

    public void setInputs(List<ConnectorDefinition> inputs) { this.inputs = inputs; }

    public List<ConnectorDefinition> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ConnectorDefinition> outputs) { this.outputs = outputs; }
}

