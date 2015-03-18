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
        Category loadBlocks = new Category("Load Blocks");
        List<Definition> loadBlockDefinitions = new ArrayList<Definition>();
        loadBlockDefinitions.add(createLoadDBBlock());
        loadBlocks.setDefinitions(loadBlockDefinitions);

        Category transformBlocks = new Category("Transform Blocks");
        List<Definition> transformBlockDefinitions = new ArrayList<Definition>();
        transformBlockDefinitions.add(createColumnsBlock());
        transformBlockDefinitions.add(createTimeSelectionBlock());
        transformBlockDefinitions.add(createMergeBlock());
        transformBlockDefinitions.add(createScaleBlock());
        transformBlockDefinitions.add(createDownSampleBlock());
        transformBlockDefinitions.add(createLagCorrelateBlock());
        transformBlocks.setDefinitions(transformBlockDefinitions);

        Category saveBlocks = new Category("Save Blocks");
        List<Definition> saveBlockDefinitions = new ArrayList<Definition>();
        saveBlockDefinitions.add(createSaveDBBlock());
        saveBlocks.setDefinitions(saveBlockDefinitions);

        Category filteringBlocks = new Category("Filtering Blocks");
        List<Definition> filteringBlockDefinitions = new ArrayList<Definition>();
        filteringBlockDefinitions.add(createRollingAverageBlock());
        filteringBlockDefinitions.add(createRollingDeviationBlock());
        filteringBlockDefinitions.add(createWeightedAverageBlock());
        filteringBlockDefinitions.add(createWeightedDeviationBlock());
        filteringBlockDefinitions.add(createSavitskyGolayFilterBlock());
        filteringBlockDefinitions.add(createExponentialFilterBlock());
        filteringBlockDefinitions.add(createStepwiseAverageBlock());
        filteringBlocks.setDefinitions(filteringBlockDefinitions);

        Category cleaningBlocks = new Category("Cleaning Blocks");
        List<Definition> cleaningBlockDefinitions = new ArrayList<Definition>();
        cleaningBlockDefinitions.add(createThreeSigmaBlock());
        cleaningBlockDefinitions.add(createOutlierScrubberBlock());
        cleaningBlockDefinitions.add(createNullScrubberBlock());
        cleaningBlocks.setDefinitions(cleaningBlockDefinitions);

        try
        {
            definitionsCollection.save(loadBlocks);
            definitionsCollection.save(transformBlocks);
            definitionsCollection.save(saveBlocks);
            definitionsCollection.save(filteringBlocks);
            definitionsCollection.save(cleaningBlocks);
        }
        catch (MongoException exception)
        {
            // TODO return failure status and message to the client
            // TODO specifically handle duplicate key error
            System.out.println("MongoException: " + exception.getMessage());
        }
    }

    //
    // Load Block Definition
    //
    private Definition createLoadDBBlock() {
        Definition loadDB = new Definition("Load DB");

        loadDB.setDescription("Loads a data set from a given project");

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        loadDB.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Project", DataType.STRING.toString(), "None"));
        parameterDefinitions.add(new ParameterDefinition("DataSet", DataType.STRING.toString(), "None"));
        loadDB.setParameterDefinitions(parameterDefinitions);

        return loadDB;
    }

    //
    // Columns Block Definition
    //
    private Definition createColumnsBlock() {
        Definition columns = new Definition("Columns");

        columns.setDescription("Selects columns from a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        columns.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        columns.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Columns", DataType.STRING.toString(), "None"));
        columns.setParameterDefinitions(parameterDefinitions);

        return columns;
    }

    //
    // Time Selection Block Definition
    //
    private Definition createTimeSelectionBlock() {
        Definition timeSelection = new Definition("Time Selection");

        timeSelection.setDescription("Selects time range of data from a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        timeSelection.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        timeSelection.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("From", DataType.TIMESTAMP.toString(), "None"));
        parameterDefinitions.add(new ParameterDefinition("To", DataType.TIMESTAMP.toString(), "None"));
        timeSelection.setParameterDefinitions(parameterDefinitions);

        return timeSelection;
    }

    //
    // Merge Block Definition
    //
    private Definition createMergeBlock() {
        Definition merge = new Definition("Merge");

        merge.setDescription("Merge data frames into one data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        merge.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        merge.setOutputConnectorDefinitions(outputConnectorDefinitions);

        return merge;
    }

    //
    // Scale Block Definition
    //
    private Definition createScaleBlock() {
        Definition scale = new Definition("Scale");

        scale.setDescription("Normalize a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        scale.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        scale.setOutputConnectorDefinitions(outputConnectorDefinitions);

        return scale;
    }

    //
    // Down Sample Block Definition
    //
    private Definition createDownSampleBlock() {
        Definition downSample = new Definition("Down Sample");

        downSample.setDescription("Down sample a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        downSample.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        downSample.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("SampleSize", DataType.INT.toString(), Integer.toString(100)));
        parameterDefinitions.add(new ParameterDefinition("To", DataType.STRING.toString(), "last"));
        downSample.setParameterDefinitions(parameterDefinitions);

        return downSample;
    }


    //
    // Lag Correlate Block Definition
    //
    private Definition createLagCorrelateBlock() {
        Definition lagCorrelate = new Definition("Lag Correlate");

        lagCorrelate.setDescription("Performs a lag correlation a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        lagCorrelate.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        lagCorrelate.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("lag", DataType.INT.toString(), Integer.toString(60)));
        lagCorrelate.setParameterDefinitions(parameterDefinitions);

        return lagCorrelate;
    }

    //
    // Save Block Definition
    //
    private Definition createSaveDBBlock() {
        Definition saveDB = new Definition("Save DB");

        saveDB.setDescription("Saves a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        saveDB.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Project", DataType.STRING.toString(), "None"));
        parameterDefinitions.add(new ParameterDefinition("DataSet", DataType.STRING.toString(), "None"));
        saveDB.setParameterDefinitions(parameterDefinitions);

        return saveDB;
    }


    //
    // Rolling Average Block Definition
    //
    private Definition createRollingAverageBlock() {
        Definition rollingAverage = new Definition("Rolling Average");

        rollingAverage.setDescription("Determines the rolling average of a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        rollingAverage.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        rollingAverage.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("WindowSize", DataType.INT.toString(), Integer.toString(60)));
        rollingAverage.setParameterDefinitions(parameterDefinitions);

        return rollingAverage;
    }

    //
    // Rolling Deviation Block Definition
    //
    private Definition createRollingDeviationBlock() {
        Definition rollingDeviation = new Definition("Rolling Deviation");

        rollingDeviation.setDescription("Determines the rolling deviation of a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        rollingDeviation.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        rollingDeviation.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("WindowSize", DataType.INT.toString(), Integer.toString(60)));
        rollingDeviation.setParameterDefinitions(parameterDefinitions);

        return rollingDeviation;
    }

    //
    // Weighted Average Block Definition
    //
    private Definition createWeightedAverageBlock() {
        Definition weightedAverage = new Definition("Weighted Average");

        weightedAverage.setDescription("Determines the weighted average of a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        weightedAverage.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        weightedAverage.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Weight", DataType.INT.toString(), Integer.toString(20)));
        weightedAverage.setParameterDefinitions(parameterDefinitions);

        return weightedAverage;
    }

    //
    // Weighted Deviation Block Definition
    //
    private Definition createWeightedDeviationBlock() {
        Definition weightedDeviation = new Definition("Weighted Deviation");

        weightedDeviation.setDescription("Determines the weighted deviation of a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        weightedDeviation.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        weightedDeviation.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Weight", DataType.INT.toString().toString(), Integer.toString(20)));
        weightedDeviation.setParameterDefinitions(parameterDefinitions);

        return weightedDeviation;
    }

    //
    // Savitsky Golay Filter Block Definition
    //
    private Definition createSavitskyGolayFilterBlock() {
        Definition savitskyGolayFilter = new Definition("Savitsky-Golay Filter");

        savitskyGolayFilter.setDescription("Apply Savitsky-Golay filter to a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        savitskyGolayFilter.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        savitskyGolayFilter.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("PointsToLeft", DataType.INT.toString().toString(), Integer.toString(10)));
        parameterDefinitions.add(new ParameterDefinition("PointsToRight", DataType.INT.toString().toString(), Integer.toString(10)));
        parameterDefinitions.add(new ParameterDefinition("PolynomialOrder", DataType.INT.toString().toString(), Integer.toString(3)));
        savitskyGolayFilter.setParameterDefinitions(parameterDefinitions);

        return savitskyGolayFilter;
    }

    //
    // Exponential Filter Block Definition
    //
    private Definition createExponentialFilterBlock() {
        Definition exponentialFilter = new Definition("Exponential Filter");

        exponentialFilter.setDescription("Apply exponential filter to a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        exponentialFilter.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        exponentialFilter.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Alpha", DataType.FLOAT.toString(), Double.toString(0.8)));
        parameterDefinitions.add(new ParameterDefinition("Order", DataType.INT.toString().toString(), Integer.toString(1)));
        exponentialFilter.setParameterDefinitions(parameterDefinitions);

        return exponentialFilter;
    }

    //
    // Stepwise Average Block Definition
    //
    private Definition createStepwiseAverageBlock() {
        Definition stepwiseAverage = new Definition("Stepwise Average");

        stepwiseAverage.setDescription("Apply stepwise average filter to a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        stepwiseAverage.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        stepwiseAverage.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("WindowSize", DataType.INT.toString().toString(), Integer.toString(20)));
        stepwiseAverage.setParameterDefinitions(parameterDefinitions);

        return stepwiseAverage;
    }

    //
    // Three Sigma Block Definition
    //
    private Definition createThreeSigmaBlock() {
        Definition threeSigma = new Definition("Three Sigma");

        threeSigma.setDescription("Apply three sigma algorithm to a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        threeSigma.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        threeSigma.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("MovingWindow", DataType.INT.toString().toString(), Double.toString(20)));
        parameterDefinitions.add(new ParameterDefinition("Order", DataType.FLOAT.toString(), Double.toString(3)));
        threeSigma.setParameterDefinitions(parameterDefinitions);

        return threeSigma;
    }

    //
    // Null Scrubber Block Definition
    //
    private Definition createNullScrubberBlock() {
        Definition nullScrubber = new Definition("Null Scrubber");

        nullScrubber.setDescription("Removes NaN values from a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        nullScrubber.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        nullScrubber.setOutputConnectorDefinitions(outputConnectorDefinitions);

        return nullScrubber;
    }

    //
    // Outlier Scrubber Definition
    //
    private Definition createOutlierScrubberBlock() {
        Definition outlierScrubber = new Definition("Outlier Scrubber");

        outlierScrubber.setDescription("Removes outlier values from a given data frame");

        List<ConnectorDefinition> inputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        outlierScrubber.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        outlierScrubber.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Algorithm", DataType.STRING.toString().toString(), "3Sigma"));
        outlierScrubber.setParameterDefinitions(parameterDefinitions);

        return outlierScrubber;
    }
}
