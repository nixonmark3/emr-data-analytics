package emr.analytics;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

import emr.analytics.models.definition.*;

public class DefinitionGenerator {
    MongoCollection _definitions = null;

    public DefinitionGenerator(MongoCollection definitionsCollection) {
        this._definitions = definitionsCollection;
    }

    public void generate() {
        createLoadDBBlock();
        createDataBrickBlock();
        createSaveDBBlock();
        createColumnsBlock();
        createTimeSelectionBlock();
        createMergeBlock();
        createScaleBlock();
        createDownSampleBlock();
        createLagCorrelateBlock();
        createTest1Block();
        createRollingAverageBlock();
        createRollingDeviationBlock();
        createWeightedAverageBlock();
        createWeightedDeviationBlock();
        createSavitskyGolayFilterBlock();
        createExponentialFilterBlock();
        createStepwiseAverageBlock();
        createThreeSigmaBlock();
        createOutlierScrubberBlock();
        createNullScrubberBlock();
    }

    //
    // Create Data Brick Block
    //
    private void createDataBrickBlock() {
        // Definition
        Definition definition = new Definition("DataBrick", "Data Brick", Category.DATA_SOURCES.toString());
        definition.setDescription("Loads a data brick");

        // Output Connectors
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        // Parameters
        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Project",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Projects",
                        new ArrayList<Argument>())));

        parameters.add(new ParameterDefinition("Query",
                DataType.QUERY.toString(),
                "None",
                new ArrayList<String>(),
                null));

        definition.setParameters(parameters);

        // Save Definition
        _definitions.save(definition);
    }

    //
    // Load Block Definition
    //
    private void createLoadDBBlock() {
        Definition loadDB = new Definition("LoadDB", "Load DB", Category.DATA_SOURCES.toString());

        loadDB.setDescription("Loads a data set from a given project");

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        loadDB.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Project",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Projects",
                        new ArrayList<Argument>())));

        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new Argument("Project", 0, "Project.Value"));

        parameters.add(new ParameterDefinition("Data Set",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "DataSets",
                        arguments)));

        loadDB.setParameters(parameters);

        _definitions.save(loadDB);
    }

    //
    // Save Block Definition
    //
    private void createSaveDBBlock() {
        Definition saveDB = new Definition("SaveDB", "Save DB", Category.DATA_SOURCES.toString());

        saveDB.setDescription("Saves a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        saveDB.setInputConnectors(inputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Project",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("DataSet",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        saveDB.setParameters(parameters);

        _definitions.save(saveDB);
    }

    //
    // Columns Block Definition
    //
    private void createColumnsBlock() {
        Definition columns = new Definition("Columns", "Columns", Category.TRANSFORMERS.toString());

        columns.setDescription("Selects columns from a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        columns.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        columns.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new Argument("BlockName", 1, "BlockName.Value"));

        parameters.add(new ParameterDefinition("Columns",
                DataType.MULTI_SELECT_LIST.toString(),
                new ArrayList<String>(),
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Columns",
                        arguments)));

        columns.setParameters(parameters);

        _definitions.save(columns);
    }

    //
    // Time Selection Block Definition
    //
    private void createTimeSelectionBlock() {
        Definition timeSelection = new Definition("TimeSelection", "Time Selection", Category.TRANSFORMERS.toString());

        timeSelection.setDescription("Selects time range of data from a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        timeSelection.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        timeSelection.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("From",
                DataType.TIMESTAMP.toString(),
                "None",
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("To",
                DataType.TIMESTAMP.toString(),
                "None",
                new ArrayList<String>(),
                null));
        timeSelection.setParameters(parameters);

        _definitions.save(timeSelection);
    }

    //
    // Merge Block Definition
    //
    private void createMergeBlock() {
        Definition merge = new Definition("Merge", "Merge", Category.TRANSFORMERS.toString());

        merge.setDescription("Merge data frames into one data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        merge.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        merge.setOutputConnectors(outputConnectors);

        _definitions.save(merge);
    }

    //
    // Scale Block Definition
    //
    private void createScaleBlock() {
        Definition scale = new Definition("Scale", "Scale", Category.TRANSFORMERS.toString());

        scale.setDescription("Normalize a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        scale.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        scale.setOutputConnectors(outputConnectors);

        _definitions.save(scale);
    }

    //
    // Down Sample Block Definition
    //
    private void createDownSampleBlock() {
        Definition downSample = new Definition("DownSample", "Down Sample", Category.TRANSFORMERS.toString());

        downSample.setDescription("Down sample a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        downSample.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        downSample.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("SampleSize",
                DataType.INT.toString(),
                Integer.toString(100),
                new ArrayList<String>(),
                null));

        List<String> opts = new ArrayList<String>();
        opts.add("First");
        opts.add("Last");
        opts.add("Mean");

        parameters.add(new ParameterDefinition("Interpolation",
                DataType.LIST.toString(), "Last",
                opts,
                null));

        downSample.setParameters(parameters);

        _definitions.save(downSample);
    }


    //
    // Lag Correlate Block Definition
    //
    private void createLagCorrelateBlock() {
        Definition lagCorrelate = new Definition("LagCorrelate", "Lag Correlate", Category.TRANSFORMERS.toString());

        lagCorrelate.setDescription("Performs a lag correlation a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        lagCorrelate.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        lagCorrelate.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("lag",
                DataType.INT.toString(),
                Integer.toString(60),
                new ArrayList<String>(),
                null));
        lagCorrelate.setParameters(parameters);

        _definitions.save(lagCorrelate);
    }

    //
    // Multi Connector Block Example
    //
    private void createTest1Block() {
        Definition test = new Definition("Test1", "Test1", Category.TRANSFORMERS.toString());

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in1", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("in2", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("in3", DataType.FRAME.toString()));
        test.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out1", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("out2", DataType.FRAME.toString()));
        test.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Prop1",
                DataType.INT.toString(),
                Integer.toString(0),
                new ArrayList<String>(),
                null));
        test.setParameters(parameters);

        _definitions.save(test);
    }

    //
    // Rolling Average Block Definition
    //
    private void createRollingAverageBlock() {
        Definition rollingAverage = new Definition("RollingAverage", "Rolling Average", Category.FILTERS.toString());

        rollingAverage.setDescription("Determines the rolling average of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        rollingAverage.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        rollingAverage.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("WindowSize",
                DataType.INT.toString(),
                Integer.toString(60),
                new ArrayList<String>(),
                null));
        rollingAverage.setParameters(parameters);

        _definitions.save(rollingAverage);
    }

    //
    // Rolling Deviation Block Definition
    //
    private void createRollingDeviationBlock() {
        Definition rollingDeviation = new Definition("RollingDeviation", "Rolling Deviation", Category.FILTERS.toString());

        rollingDeviation.setDescription("Determines the rolling deviation of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        rollingDeviation.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        rollingDeviation.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("WindowSize",
                DataType.INT.toString(),
                Integer.toString(60),
                new ArrayList<String>(),
                null));
        rollingDeviation.setParameters(parameters);

        _definitions.save(rollingDeviation);
    }

    //
    // Weighted Average Block Definition
    //
    private void createWeightedAverageBlock() {
        Definition weightedAverage = new Definition("WeightedAverage", "Weighted Average", Category.FILTERS.toString());

        weightedAverage.setDescription("Determines the weighted average of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        weightedAverage.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        weightedAverage.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Weight",
                DataType.INT.toString(),
                Integer.toString(20),
                new ArrayList<String>(),
                null));
        weightedAverage.setParameters(parameters);

        _definitions.save(weightedAverage);
    }

    //
    // Weighted Deviation Block Definition
    //
    private void createWeightedDeviationBlock() {
        Definition weightedDeviation = new Definition("WeightedDeviation", "Weighted Deviation", Category.FILTERS.toString());

        weightedDeviation.setDescription("Determines the weighted deviation of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        weightedDeviation.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        weightedDeviation.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Weight",
                DataType.INT.toString().toString(),
                Integer.toString(20),
                new ArrayList<String>(),
                null));
        weightedDeviation.setParameters(parameters);

        _definitions.save(weightedDeviation);
    }

    //
    // Savitsky Golay Filter Block Definition
    //
    private void createSavitskyGolayFilterBlock() {
        Definition savitskyGolayFilter = new Definition("SavitskyGolayFilter", "Savitsky-Golay Filter", Category.FILTERS.toString());

        savitskyGolayFilter.setDescription("Apply Savitsky-Golay filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        savitskyGolayFilter.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        savitskyGolayFilter.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("PointsToLeft", DataType.INT.toString().toString(), Integer.toString(10),
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("PointsToRight", DataType.INT.toString().toString(), Integer.toString(10), new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("PolynomialOrder", DataType.INT.toString().toString(), Integer.toString(3), new ArrayList<String>(),
                null));
        savitskyGolayFilter.setParameters(parameters);

        _definitions.save(savitskyGolayFilter);
    }

    //
    // Exponential Filter Block Definition
    //
    private void createExponentialFilterBlock() {
        Definition exponentialFilter = new Definition("ExponentialFilter", "Exponential Filter", Category.FILTERS.toString());

        exponentialFilter.setDescription("Apply exponential filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        exponentialFilter.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        exponentialFilter.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Alpha", DataType.FLOAT.toString(), Double.toString(0.8), new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Order", DataType.INT.toString().toString(), Integer.toString(1), new ArrayList<String>(),
                null));
        exponentialFilter.setParameters(parameters);

        _definitions.save(exponentialFilter);
    }

    //
    // Stepwise Average Block Definition
    //
    private void createStepwiseAverageBlock() {
        Definition stepwiseAverage = new Definition("StepwiseAverage", "Stepwise Average", Category.FILTERS.toString());

        stepwiseAverage.setDescription("Apply stepwise average filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        stepwiseAverage.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        stepwiseAverage.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("WindowSize", DataType.INT.toString().toString(), Integer.toString(20), new ArrayList<String>(),
                null));
        stepwiseAverage.setParameters(parameters);

        _definitions.save(stepwiseAverage);
    }

    //
    // Three Sigma Block Definition
    //
    private void createThreeSigmaBlock() {
        Definition threeSigma = new Definition("ThreeSigma", "Three Sigma", Category.CLEANERS.toString());

        threeSigma.setDescription("Apply three sigma algorithm to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        threeSigma.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        threeSigma.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("MovingWindow", DataType.INT.toString().toString(), Double.toString(20), new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Order", DataType.FLOAT.toString(), Double.toString(3), new ArrayList<String>(),
                null));
        threeSigma.setParameters(parameters);

        _definitions.save(threeSigma);
    }

    //
    // Null Scrubber Block Definition
    //
    private void createNullScrubberBlock() {
        Definition nullScrubber = new Definition("NullScrubber", "Null Scrubber", Category.CLEANERS.toString());

        nullScrubber.setDescription("Removes NaN values from a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        nullScrubber.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        nullScrubber.setOutputConnectors(outputConnectors);

        _definitions.save(nullScrubber);
    }

    //
    // Outlier Scrubber Definition
    //
    private void createOutlierScrubberBlock() {
        Definition outlierScrubber = new Definition("OutlierScrubber", "Outlier Scrubber", Category.CLEANERS.toString());

        outlierScrubber.setDescription("Removes outlier values from a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        outlierScrubber.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        outlierScrubber.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Algorithm", DataType.STRING.toString().toString(), "3Sigma", new ArrayList<String>(),
                null));
        outlierScrubber.setParameters(parameters);

        _definitions.save(outlierScrubber);
    }
}
