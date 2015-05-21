package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.Category;
import emr.analytics.models.definition.ConnectorDefinition;
import emr.analytics.models.definition.DataType;
import emr.analytics.models.definition.Definition;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class DropNaN implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("DropNa", "Drop NaN", Category.FILTERS.toString());

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        definition.setOutputConnectors(outputConnectors);

        definitions.save(definition);
    }
}
