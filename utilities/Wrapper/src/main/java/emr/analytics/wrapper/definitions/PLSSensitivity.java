package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.Category;
import emr.analytics.models.definition.ConnectorDefinition;
import emr.analytics.models.definition.DataType;
import emr.analytics.models.definition.Definition;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class PLSSensitivity implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition = new Definition("Sensitivity", "PLSSensitivity", Category.TRANSFORMERS.toString());
        definition.setOnlineComplement("Predict");

        definition.setDescription("Calculates sensitivity of output y to set of inputs X using PLS");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("y", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("model", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("coefs", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("r2", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("ycomp", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        definitions.save(definition);
    }
}
