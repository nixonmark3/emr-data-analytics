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

        /* ALL ALGORITHMS MUST BE GENERATED WITH THERE CATEGORIES */
        generateDataSources();
        generateFilters();
        generateTransformers();
    }

    private void generateDataSources() {

        createAddDataSetBlock();
        createCreateDBBlock();
        createDataBrickBlock();
        createLoadCSVBlock();
        createLoadDBBlock();
        createSaveCSVBlock();
        createSaveDBBlock();
        createKafkaBlock();
    }

    private void generateFilters() {

        createBoxcarAverageBlock();
        createEWMABlock();
        createEWMStDevBlock();
        createExponentialFilterBlock();
        createSavGolayBlock();
        createRollingAveBlock();
        createRollingStdDevBlock();
        createThreeSigmaBlock();
    }

    private void generateTransformers() {

        createColumnsBlock();
        createDownSampleBlock();
        createMergeBlock();
        createLagCorrelateBlock();
        createPLSSensitivityBlock();
        createScaleBlock();
        createPLSBlock();
        createWebServicePostBlock();
        createSplitBlock();
        createTimeDelayBlock();
        createShiftBlock();
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
                        "Bricks",
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

        List<String> opts = new ArrayList<String>();
        opts.add("True");
        opts.add("False");

        parameters.add(new ParameterDefinition("Plot",
                DataType.LIST.toString(),
                "False",
                opts,
                null));

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

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        saveDB.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Project",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Projects",
                        new ArrayList<Argument>())));

        parameters.add(new ParameterDefinition("Data Set Name",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        saveDB.setParameters(parameters);

        _definitions.save(saveDB);
    }

    //
    // Load Block Definition
    //
    private void createLoadCSVBlock() {
        Definition load = new Definition("LoadCSV", "Load CSV", Category.DATA_SOURCES.toString());

        load.setDescription("Loads a data set from a csv file");

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        load.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Filename",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        List<String> opts = new ArrayList<String>();
        opts.add("True");
        opts.add("False");

        parameters.add(new ParameterDefinition("Plot",
                DataType.LIST.toString(),
                "False",
                opts,
                null));

        load.setParameters(parameters);

        _definitions.save(load);
    }

    //
    // Save CSV Block Definition
    //
    private void createSaveCSVBlock() {
        Definition save = new Definition("SaveCSV", "Save CSV", Category.DATA_SOURCES.toString());

        save.setDescription("Saves a given data frame to CSV");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        save.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        save.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Filename",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        save.setParameters(parameters);

        _definitions.save(save);
    }

    //
    // Create DB Block Definition
    //
    private void createCreateDBBlock() {
        Definition createDB = new Definition("CreateDB", "Create DB", Category.DATA_SOURCES.toString());

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Filename",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Project Name",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Data Set Name",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        createDB.setParameters(parameters);

        _definitions.save(createDB);
    }

    //
    // Add Data Set Block Definition
    //
    private void createAddDataSetBlock() {
        Definition addDataSet = new Definition("AddDataSet", "Add Data Set", Category.DATA_SOURCES.toString());

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Filename",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Project Name",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Projects",
                        new ArrayList<Argument>())));

        parameters.add(new ParameterDefinition("Data Set Name",
                DataType.STRING.toString(),
                "None",
                new ArrayList<String>(),
                null));

        addDataSet.setParameters(parameters);

        _definitions.save(addDataSet);
    }

    private void createKafkaBlock(){

        Definition definition;

        definition = new Definition("Kafka", "Kafka Data", Category.DATA_SOURCES.toString());
        definition.setDescription("Spark streaming block that monitors a topic in Kafka.");
        definition.setOnlineOnly(true);
        definition.setSignature(new Signature("emr.analytics.spark.algorithms.Utilities",
            "Utilities",
            "kafkaStream",
            new String[]{
                "ssc",
                "parameter:Zookeeper Quorum",
                "appName",
                "parameter:Topics"
            })
        );

        // add output connector
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Zookeeper Quorum",
                DataType.STRING.toString(),
                "localhost:2181",
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Topics",
                DataType.STRING.toString(),
                "runtime",
                new ArrayList<String>(),
                null));

        definition.setParameters(parameters);

        _definitions.save(definition);
    }

    //
    // Columns Block Definition
    //
    private void createColumnsBlock() {
        Definition columns = new Definition("Columns", "Columns", Category.TRANSFORMERS.toString());

        columns.setDescription("Selects columns from a given data frame");

        columns.setSignature(new Signature("emr.analytics.spark.algorithms.Utilities",
            "Utilities",
            "columns",
            new String[]{
                "input:in",
                "parameter:Columns"
            })
        );

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
    // Columns Block Definition
    //
    private void createPLSSensitivityBlock() {
        Definition plsSensitivity = new Definition("Sensitivity", "PLSSensitivity", Category.TRANSFORMERS.toString());
        plsSensitivity.setOnlineComplement("PLS");

        plsSensitivity.setDescription("Calculates sensitivity of output y to set of inputs X using PLS");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("y", DataType.FRAME.toString()));
        plsSensitivity.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("obj", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("coef", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("r2", DataType.FRAME.toString()));
        plsSensitivity.setOutputConnectors(outputConnectors);

        _definitions.save(plsSensitivity);
    }

//    //
//    // Time Selection Block Definition
//    //
//    private void createTimeSelectionBlock() {
//        Definition timeSelection = new Definition("TimeSelection", "Time Selection", Category.TRANSFORMERS.toString());
//
//        timeSelection.setDescription("Selects time range of data from a given data frame");
//
//        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
//        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
//        timeSelection.setInputConnectors(inputConnectors);
//
//        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
//        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
//        timeSelection.setOutputConnectors(outputConnectors);
//
//        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
//        parameters.add(new ParameterDefinition("From",
//                DataType.TIMESTAMP.toString(),
//                "None",
//                new ArrayList<String>(),
//                null));
//        parameters.add(new ParameterDefinition("To",
//                DataType.TIMESTAMP.toString(),
//                "None",
//                new ArrayList<String>(),
//                null));
//        timeSelection.setParameters(parameters);
//
//        _definitions.save(timeSelection);
//    }

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
                100,
                new ArrayList<String>(),
                null));

        List<String> opts = new ArrayList<String>();
        opts.add("First");
        opts.add("Last");
        opts.add("Mean");

        parameters.add(new ParameterDefinition("Interpolation",
                DataType.LIST.toString(),
                "Last",
                opts,
                null));

        downSample.setParameters(parameters);

        _definitions.save(downSample);
    }

    //
    // Lag Correlate Block Definition
    //
    private void createLagCorrelateBlock() {
        Definition lagCorrelate = new Definition("LagCorr", "Lag Correlate", Category.TRANSFORMERS.toString());

        lagCorrelate.setDescription("Performs a lag correlation a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        lagCorrelate.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        lagCorrelate.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Lag",
                DataType.INT.toString(),
                60,
                new ArrayList<String>(),
                null));
        lagCorrelate.setParameters(parameters);

        _definitions.save(lagCorrelate);
    }

    private void createPLSBlock(){

        Definition definition;

        definition = new Definition("PLS", "PLS Predict", Category.TRANSFORMERS.toString());
        definition.setDescription("Uses PLS model for prediction.");
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

        _definitions.save(definition);
    }

    private void createWebServicePostBlock(){

        Definition definition;

        definition = new Definition("RESTPost", "REST POST", Category.TRANSFORMERS.toString());
        definition.setDescription("Post data to a REST API.");
        definition.setOnlineOnly(true);
        definition.setSignature(new Signature("emr.analytics.spark.algorithms.Requests",
            "Requests",
            "postOpcValue",
            new String[]{
                    "parameter:Url",
                    "parameter:Tag",
                    "input:in"
            })
        );

        // add input connector
        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.STRING.toString()));
        definition.setInputConnectors(inputConnectors);

        // todo: add parameters for url, payload pattern
        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Url",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Tag",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));

        definition.setParameters(parameters);

        _definitions.save(definition);
    }

    private void createSplitBlock() {

        Definition definition = null;

        definition = new Definition("Split", "Split", Category.TRANSFORMERS.toString());
        definition.setDescription("Splits a data frame into training and testing data frames");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("train", DataType.FRAME.toString()));
        outputConnectors.add(new ConnectorDefinition("test", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Split",
                DataType.INT.toString(),
                75,
                new ArrayList<String>(),
                null));
        definition.setParameters(parameters);

        _definitions.save(definition);
    }

    private void createTimeDelayBlock() {

        Definition definition = null;

        definition = new Definition("TimeDelay", "Time Delay", Category.TRANSFORMERS.toString());;

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("x", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("y", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Max Lag",
                DataType.INT.toString(),
                10,
                new ArrayList<String>(),
                null));
        definition.setParameters(parameters);

        _definitions.save(definition);
    }

    private void createShiftBlock() {

        Definition definition = null;

        definition = new Definition("Shift", "Time Shift", Category.TRANSFORMERS.toString());;

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        inputConnectors.add(new ConnectorDefinition("delay", DataType.FRAME.toString()));
        definition.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        definition.setOutputConnectors(outputConnectors);

        _definitions.save(definition);
    }

    //
    // Rolling Average Block Definition
    //
    private void createRollingAveBlock() {
        Definition rollingAverage = new Definition("RollingAve", "Rolling Ave", Category.FILTERS.toString());

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
                60,
                new ArrayList<String>(),
                null));
        rollingAverage.setParameters(parameters);

        _definitions.save(rollingAverage);
    }

    //
    // Rolling Deviation Block Definition
    //
    private void createRollingStdDevBlock() {
        Definition rollingDeviation = new Definition("RollingStdDev", "Rolling Std Dev", Category.FILTERS.toString());

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
                60,
                new ArrayList<String>(),
                null));
        rollingDeviation.setParameters(parameters);

        _definitions.save(rollingDeviation);
    }

    //
    // Weighted Average Block Definition
    //
    private void createEWMABlock() {
        Definition EWMA = new Definition("EWMA", "Weighted Ave", Category.FILTERS.toString());

        EWMA.setDescription("Determines the exponentially weighted moving average of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString()));
        EWMA.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        EWMA.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Weight",
                DataType.INT.toString(),
                20,
                new ArrayList<String>(),
                null));
        EWMA.setParameters(parameters);

        _definitions.save(EWMA);
    }

    //
    // Weighted Deviation Block Definition
    //
    private void createEWMStDevBlock() {
        Definition EWMStDev = new Definition("EWMStDev", "Weighted Dev", Category.FILTERS.toString());

        EWMStDev.setDescription("Determines the exponentially weighted  deviation of a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        EWMStDev.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        EWMStDev.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Weight",
                DataType.INT.toString().toString(),
                20,
                new ArrayList<String>(),
                null));
        EWMStDev.setParameters(parameters);

        _definitions.save(EWMStDev);
    }

    //
    // Savitsky Golay Filter Block Definition
    //
    private void createSavGolayBlock() {
        Definition SavGolay = new Definition("SavGolay", "Savitsky-Golay Filter", Category.FILTERS.toString());

        SavGolay.setDescription("Apply Savitsky-Golay filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        SavGolay.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        SavGolay.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("PointsToLeft",
                DataType.INT.toString().toString(),
                10,
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("PointsToRight",
                DataType.INT.toString().toString(),
                10,
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("PolynomialOrder",
                DataType.INT.toString().toString(),
                3, new ArrayList<String>(),
                null));
        SavGolay.setParameters(parameters);

        _definitions.save(SavGolay);
    }

    //
    // Exponential Filter Block Definition
    //
    private void createExponentialFilterBlock() {
        Definition exponentialFilter = new Definition("ExpFilter", "Exp Filter", Category.FILTERS.toString());

        exponentialFilter.setDescription("Apply exponential filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        exponentialFilter.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        exponentialFilter.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Alpha",
                DataType.FLOAT.toString(),
                0.8,
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("Order",
                DataType.INT.toString().toString(),
                1,
                new ArrayList<String>(),
                null));
        exponentialFilter.setParameters(parameters);

        _definitions.save(exponentialFilter);
    }

    //
    // Boxcar Average Block Definition
    //
    private void createBoxcarAverageBlock() {
        Definition BoxcarAverage = new Definition("BoxcarAve", "Boxcar Ave", Category.FILTERS.toString());

        BoxcarAverage.setDescription("Apply Boxcar average filter to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        BoxcarAverage.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        BoxcarAverage.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("WindowSize",
                DataType.INT.toString().toString(),
                20,
                new ArrayList<String>(),
                null));
        BoxcarAverage.setParameters(parameters);

        _definitions.save(BoxcarAverage);
    }

    //
    // Three Sigma Block Definition
    //
    private void createThreeSigmaBlock() {
        Definition threeSigma = new Definition("ThreeSigma", "Three Sigma", Category.FILTERS.toString());

        threeSigma.setDescription("Apply three (or g-order) sigma algorithm to a given data frame");

        List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
        inputConnectors.add(new ConnectorDefinition("in", DataType.FRAME.toString().toString()));
        threeSigma.setInputConnectors(inputConnectors);

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString().toString()));
        threeSigma.setOutputConnectors(outputConnectors);

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("MovingWindow",
                DataType.INT.toString().toString(),
                20,
                new ArrayList<String>(),
                null));
        parameters.add(new ParameterDefinition("g-Sigma",
                DataType.FLOAT.toString(),
                3.0,
                new ArrayList<String>(),
                null));
        threeSigma.setParameters(parameters);

        _definitions.save(threeSigma);
    }
}
