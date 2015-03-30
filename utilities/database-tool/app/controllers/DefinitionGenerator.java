package controllers;

import com.mongodb.MongoException;

import models.definition.*;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

public class DefinitionGenerator {
    public DefinitionGenerator() {}

    public void generate(MongoCollection definitionsCollection)
    {
        Category loadBlocks = new Category("Data Sources");
        List<Definition> loadBlockDefinitions = new ArrayList<Definition>();
        loadBlockDefinitions.add(createLoadDBBlock());
        loadBlockDefinitions.add(createSaveDBBlock());
        loadBlocks.setDefinitions(loadBlockDefinitions);

        Category transformBlocks = new Category("Transformers");
        List<Definition> transformBlockDefinitions = new ArrayList<Definition>();
        transformBlockDefinitions.add(createColumnsBlock());
        transformBlockDefinitions.add(createTimeSelectionBlock());
        transformBlockDefinitions.add(createMergeBlock());
        transformBlockDefinitions.add(createScaleBlock());
        transformBlockDefinitions.add(createDownSampleBlock());
        transformBlockDefinitions.add(createLagCorrelateBlock());
        transformBlockDefinitions.add(createTest1Block());
        transformBlocks.setDefinitions(transformBlockDefinitions);

        Category filteringBlocks = new Category("Filters");
        List<Definition> filteringBlockDefinitions = new ArrayList<Definition>();
        filteringBlockDefinitions.add(createRollingAverageBlock());
        filteringBlockDefinitions.add(createRollingDeviationBlock());
        filteringBlockDefinitions.add(createWeightedAverageBlock());
        filteringBlockDefinitions.add(createWeightedDeviationBlock());
        filteringBlockDefinitions.add(createSavitskyGolayFilterBlock());
        filteringBlockDefinitions.add(createExponentialFilterBlock());
        filteringBlockDefinitions.add(createStepwiseAverageBlock());
        filteringBlocks.setDefinitions(filteringBlockDefinitions);

        Category cleaningBlocks = new Category("Cleaners");
        List<Definition> cleaningBlockDefinitions = new ArrayList<Definition>();
        cleaningBlockDefinitions.add(createThreeSigmaBlock());
        cleaningBlockDefinitions.add(createOutlierScrubberBlock());
        cleaningBlockDefinitions.add(createNullScrubberBlock());
        cleaningBlocks.setDefinitions(cleaningBlockDefinitions);

        try
        {
            definitionsCollection.save(loadBlocks);
            definitionsCollection.save(transformBlocks);
            definitionsCollection.save(filteringBlocks);
            definitionsCollection.save(cleaningBlocks);
        }
        catch (MongoException exception)
        {
            exception.printStackTrace();
        }
    }

    //
    // Load Block Definition
    //
    private Definition createLoadDBBlock() {
        Definition loadDB = new Definition("Load DB");

        loadDB.setDescription("Loads a data set from a given project");

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        loadDB.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        ParameterOptions projOptions = new ParameterOptions();
        projOptions.setIsDependent(false);
        List<String> dependants = new ArrayList<String>();
        dependants.add("Data Set");
        projOptions.setDependants(dependants);
        projOptions.setInputType("DropDown");
        projOptions.setDynamic(true);
        List<String> projOpts = new ArrayList<String>();
        projOpts.add("findProjects");
        projOptions.setFieldOptions(projOpts);

        parameters.add(new ParameterDefinition("Project", DataType.STRING.toString(), "None", projOptions));

        ParameterOptions dsOptions = new ParameterOptions();
        dsOptions.setIsDependent(true);
        dsOptions.setInputType("DropDown");
        dsOptions.setDynamic(true);
        List<String> dsOpts = new ArrayList<String>();
        dsOpts.add("findDataSets");
        dsOptions.setFieldOptions(dsOpts);

        parameters.add(new ParameterDefinition("Data Set", DataType.STRING.toString(), "None", dsOptions));
        loadDB.setParameters(parameters);

        return loadDB;
    }

    //
    // Columns Block Definition
    //
    private Definition createColumnsBlock() {
        Definition columns = new Definition("Columns");

        columns.setDescription("Selects columns from a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        columns.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        columns.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Columns", DataType.STRING.toString(), "None", new ParameterOptions()));
        columns.setParameters(parameters);

        return columns;
    }

    //
    // Time Selection Block Definition
    //
    private Definition createTimeSelectionBlock() {
        Definition timeSelection = new Definition("Time Selection");

        timeSelection.setDescription("Selects time range of data from a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        timeSelection.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        timeSelection.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("From", DataType.TIMESTAMP.toString(), "None", new ParameterOptions()));
        parameters.add(new ParameterDefinition("To", DataType.TIMESTAMP.toString(), "None", new ParameterOptions()));
        timeSelection.setParameters(parameters);

        return timeSelection;
    }

    //
    // Merge Block Definition
    //
    private Definition createMergeBlock() {
        Definition merge = new Definition("Merge");

        merge.setDescription("Merge data frames into one data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        merge.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        merge.setOutputConnectors(outputConnectors);

        return merge;
    }

    //
    // Scale Block Definition
    //
    private Definition createScaleBlock() {
        Definition scale = new Definition("Scale");

        scale.setDescription("Normalize a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        scale.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        scale.setOutputConnectors(outputConnectors);

        return scale;
    }

    //
    // Down Sample Block Definition
    //
    private Definition createDownSampleBlock() {
        Definition downSample = new Definition("Down Sample");

        downSample.setDescription("Down sample a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        downSample.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        downSample.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("SampleSize", DataType.INT.toString(), Integer.toString(100), new ParameterOptions()));

        ParameterOptions options = new ParameterOptions();
        options.setIsDependent(false);
        options.setInputType("DropDown");
        options.setDynamic(false);
        List<String> opts = new ArrayList<String>();
        opts.add("First");
        opts.add("Last");
        opts.add("Mean");
        options.setFieldOptions(opts);
        parameters.add(new ParameterDefinition("Interpolation", DataType.STRING.toString(), "Last", options));
        downSample.setParameters(parameters);

        return downSample;
    }


    //
    // Lag Correlate Block Definition
    //
    private Definition createLagCorrelateBlock() {
        Definition lagCorrelate = new Definition("Lag Correlate");

        lagCorrelate.setDescription("Performs a lag correlation a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        lagCorrelate.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        lagCorrelate.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("lag", DataType.INT.toString(), Integer.toString(60), new ParameterOptions()));
        lagCorrelate.setParameters(parameters);

        return lagCorrelate;
    }

    //
    // Save Block Definition
    //
    private Definition createSaveDBBlock() {
        Definition saveDB = new Definition("Save DB");

        saveDB.setDescription("Saves a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        saveDB.setInputConnectors(inputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Project", DataType.STRING.toString(), "None", new ParameterOptions()));
        parameters.add(new ParameterDefinition("DataSet", DataType.STRING.toString(), "None", new ParameterOptions()));
        saveDB.setParameters(parameters);

        return saveDB;
    }


    //
    // Rolling Average Block Definition
    //
    private Definition createRollingAverageBlock() {
        Definition rollingAverage = new Definition("Rolling Average");

        rollingAverage.setDescription("Determines the rolling average of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        rollingAverage.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        rollingAverage.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("WindowSize", DataType.INT.toString(), Integer.toString(60), new ParameterOptions()));
        rollingAverage.setParameters(parameters);

        return rollingAverage;
    }

    //
    // Rolling Deviation Block Definition
    //
    private Definition createRollingDeviationBlock() {
        Definition rollingDeviation = new Definition("Rolling Deviation");

        rollingDeviation.setDescription("Determines the rolling deviation of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        rollingDeviation.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        rollingDeviation.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("WindowSize", DataType.INT.toString(), Integer.toString(60), new ParameterOptions()));
        rollingDeviation.setParameters(parameters);

        return rollingDeviation;
    }

    //
    // Weighted Average Block Definition
    //
    private Definition createWeightedAverageBlock() {
        Definition weightedAverage = new Definition("Weighted Average");

        weightedAverage.setDescription("Determines the weighted average of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        weightedAverage.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        weightedAverage.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Weight", DataType.INT.toString(), Integer.toString(20), new ParameterOptions()));
        weightedAverage.setParameters(parameters);

        return weightedAverage;
    }

    //
    // Weighted Deviation Block Definition
    //
    private Definition createWeightedDeviationBlock() {
        Definition weightedDeviation = new Definition("Weighted Deviation");

        weightedDeviation.setDescription("Determines the weighted deviation of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        weightedDeviation.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        weightedDeviation.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Weight", DataType.INT.toString().toString(), Integer.toString(20), new ParameterOptions()));
        weightedDeviation.setParameters(parameters);

        return weightedDeviation;
    }

    //
    // Savitsky Golay Filter Block Definition
    //
    private Definition createSavitskyGolayFilterBlock() {
        Definition savitskyGolayFilter = new Definition("Savitsky-Golay Filter");

        savitskyGolayFilter.setDescription("Apply Savitsky-Golay filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        savitskyGolayFilter.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        savitskyGolayFilter.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("PointsToLeft", DataType.INT.toString().toString(), Integer.toString(10), new ParameterOptions()));
        parameters.add(new ParameterDefinition("PointsToRight", DataType.INT.toString().toString(), Integer.toString(10), new ParameterOptions()));
        parameters.add(new ParameterDefinition("PolynomialOrder", DataType.INT.toString().toString(), Integer.toString(3), new ParameterOptions()));
        savitskyGolayFilter.setParameters(parameters);

        return savitskyGolayFilter;
    }

    //
    // Exponential Filter Block Definition
    //
    private Definition createExponentialFilterBlock() {
        Definition exponentialFilter = new Definition("Exponential Filter");

        exponentialFilter.setDescription("Apply exponential filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        exponentialFilter.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        exponentialFilter.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Alpha", DataType.FLOAT.toString(), Double.toString(0.8), new ParameterOptions()));
        parameters.add(new ParameterDefinition("Order", DataType.INT.toString().toString(), Integer.toString(1), new ParameterOptions()));
        exponentialFilter.setParameters(parameters);

        return exponentialFilter;
    }

    //
    // Stepwise Average Block Definition
    //
    private Definition createStepwiseAverageBlock() {
        Definition stepwiseAverage = new Definition("Stepwise Average");

        stepwiseAverage.setDescription("Apply stepwise average filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        stepwiseAverage.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        stepwiseAverage.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("WindowSize", DataType.INT.toString().toString(), Integer.toString(20), new ParameterOptions()));
        stepwiseAverage.setParameters(parameters);

        return stepwiseAverage;
    }

    //
    // Three Sigma Block Definition
    //
    private Definition createThreeSigmaBlock() {
        Definition threeSigma = new Definition("Three Sigma");

        threeSigma.setDescription("Apply three sigma algorithm to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        threeSigma.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        threeSigma.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("MovingWindow", DataType.INT.toString().toString(), Double.toString(20), new ParameterOptions()));
        parameters.add(new ParameterDefinition("Order", DataType.FLOAT.toString(), Double.toString(3), new ParameterOptions()));
        threeSigma.setParameters(parameters);

        return threeSigma;
    }

    //
    // Null Scrubber Block Definition
    //
    private Definition createNullScrubberBlock() {
        Definition nullScrubber = new Definition("Null Scrubber");

        nullScrubber.setDescription("Removes NaN values from a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        nullScrubber.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        nullScrubber.setOutputConnectors(outputConnectors);

        return nullScrubber;
    }

    //
    // Outlier Scrubber Definition
    //
    private Definition createOutlierScrubberBlock() {
        Definition outlierScrubber = new Definition("Outlier Scrubber");

        outlierScrubber.setDescription("Removes outlier values from a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        outlierScrubber.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        outlierScrubber.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Algorithm", DataType.STRING.toString().toString(), "3Sigma", new ParameterOptions()));
        outlierScrubber.setParameters(parameters);

        return outlierScrubber;
    }

    private Definition createTest1Block() {
        Definition test = new Definition("Test1");

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
        parameters.add(new ParameterDefinition("Prop1", DataType.INT.toString(), Integer.toString(0), new ParameterOptions()));
        test.setParameters(parameters);

        return test;
    }
}
