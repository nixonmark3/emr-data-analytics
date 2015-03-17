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
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        loadDB.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Project", DataType.String, "None"));
        parameterDefinitions.add(new ParameterDefinition("DataSet", DataType.String, "None"));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        columns.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        columns.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Columns", DataType.String, "None"));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        timeSelection.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        timeSelection.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("From", DataType.Timestamp, "None"));
        parameterDefinitions.add(new ParameterDefinition("To", DataType.Timestamp, "None"));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        merge.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        scale.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        downSample.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        downSample.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("SampleSize", DataType.Int, Integer.toString(100)));
        parameterDefinitions.add(new ParameterDefinition("To", DataType.String, "last"));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        lagCorrelate.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        lagCorrelate.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("lag", DataType.Int, Integer.toString(60)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        saveDB.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Project", DataType.String, "None"));
        parameterDefinitions.add(new ParameterDefinition("DataSet", DataType.String, "None"));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        rollingAverage.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        rollingAverage.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("WindowSize", DataType.Int, Integer.toString(60)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        rollingDeviation.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        rollingDeviation.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("WindowSize", DataType.Int, Integer.toString(60)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        weightedAverage.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        weightedAverage.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Weight", DataType.Int, Integer.toString(20)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        weightedDeviation.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        weightedDeviation.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Weight", DataType.Int, Integer.toString(20)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        savitskyGolayFilter.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        savitskyGolayFilter.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("PointsToLeft", DataType.Int, Integer.toString(10)));
        parameterDefinitions.add(new ParameterDefinition("PointsToRight", DataType.Int, Integer.toString(10)));
        parameterDefinitions.add(new ParameterDefinition("PolynomialOrder", DataType.Int, Integer.toString(3)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        exponentialFilter.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        exponentialFilter.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Alpha", DataType.Float, Double.toString(0.8)));
        parameterDefinitions.add(new ParameterDefinition("Order", DataType.Int, Integer.toString(1)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        stepwiseAverage.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        stepwiseAverage.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("WindowSize", DataType.Int, Integer.toString(20)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        threeSigma.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        threeSigma.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("MovingWindow", DataType.Int, Double.toString(20)));
        parameterDefinitions.add(new ParameterDefinition("Order", DataType.Float, Double.toString(3)));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        nullScrubber.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
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
        inputConnectorDefinitions.add(new ConnectorDefinition("in", DataType.DataFrame));
        outlierScrubber.setInputConnectorDefinitions(inputConnectorDefinitions);

        List<ConnectorDefinition> outputConnectorDefinitions = new ArrayList<ConnectorDefinition>();
        outputConnectorDefinitions.add(new ConnectorDefinition("out", DataType.DataFrame));
        outlierScrubber.setOutputConnectorDefinitions(outputConnectorDefinitions);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinition("Algorithm", DataType.String, "3Sigma"));
        outlierScrubber.setParameterDefinitions(parameterDefinitions);

        return outlierScrubber;
    }
}
