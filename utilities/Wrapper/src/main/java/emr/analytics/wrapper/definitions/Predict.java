package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.IExport;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class Predict implements IExport {

    public void export(MongoCollection definitions) {

        Definition definition;

        definition = new Definition("Predict", "Predict", Category.TRANSFORMERS.toString());
        definition.setOnlineOnly(true);
        definition.setSignature(new Signature("emr.analytics.spark.algorithms.Utilities",
                        "Utilities",
                        "dotProduct",
                        new String[]{
                                "input:x",
                                "block:model"
                        })
        );

        // add input connector
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        // add output connector
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FLOAT.toString()));
        definition.setOutputConnectors(outputConnectors);

        definitions.save(definition);
    }
}
