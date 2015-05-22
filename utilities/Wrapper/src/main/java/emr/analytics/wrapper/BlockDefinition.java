package emr.analytics.wrapper;

import emr.analytics.models.definition.ConnectorDefinition;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.ParameterDefinition;
import emr.analytics.models.definition.Signature;

import org.jongo.MongoCollection;

import java.util.List;

public abstract class BlockDefinition implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = createDefinition();

        definition.setSignature(createSignature());
        definition.setInputConnectors(createInputConnectors());
        definition.setOutputConnectors(createOutputConnectors());
        definition.setParameters(createParameters());

        definitions.save(definition);
    }

    public abstract Definition createDefinition();
    public abstract Signature createSignature();
    public abstract List<ConnectorDefinition> createInputConnectors();
    public abstract List<ConnectorDefinition> createOutputConnectors();
    public abstract List<ParameterDefinition> createParameters();
}
