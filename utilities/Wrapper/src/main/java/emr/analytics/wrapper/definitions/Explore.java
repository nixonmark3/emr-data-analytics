package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class Explore extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition;

        definition = new Definition(DefinitionType.CHART, "Explore", "Explore", Category.VISUALIZATIONS.toString());
        definition.setDescription("Explore datasets.");

        return definition;
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        return inputConnectors;
    }

    @Override
    public List<ConnectorDefinition> createOutputConnectors() {

        return null;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

        return null;
    }

    @Override
    public Signature createSignature() {

        return null;
    }

}
