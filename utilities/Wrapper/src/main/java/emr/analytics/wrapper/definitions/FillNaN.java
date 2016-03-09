package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class FillNaN extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        return new Definition("FillNa", "Fill NaN", Category.FILTERS.toString());
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

    public Signature createSignature() {

        return new Signature("input:in", new Operation[] {
                new Operation(Operation.OperationType.MAP,
                        "Identity",
                        "identity",
                        new String[]{
                                "lambda:x"
                        })
        });
    }

    @Override
    public ModeDefinition createOnlineMode(){

        return null;
    }

    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        return inputConnectors;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        List<String> opts = new ArrayList<String>();
        opts.add("backfill");
        opts.add("bfill");
        opts.add("pad");
        opts.add("ffill");
        opts.add("mean");

        parameters.add(new ParameterDefinition("Fill Method",
                ParameterType.ENUMERATION,
                ValueType.SCALAR,
                "ffill",
                opts,
                null));

        return parameters;
    }

}