package emr.analytics.service.interpreters;

import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.messages.Describe;
import emr.analytics.models.messages.Feature;
import emr.analytics.models.messages.Features;
import emr.analytics.service.messages.DataFrameSchema;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.log4j.PropertyConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PySparkInterpreter extends PythonInterpreter implements ExecuteResultHandler {

    protected SparkConf sparkConf;
    protected JavaSparkContext sparkContext;
    protected SQLContext sqlContext;

    public PySparkInterpreter(String name, InterpreterNotificationHandler notificationHandler){
        super(notificationHandler);

        loadProperties("spark");
        loadProperties("pyspark");

        loadLogProperties();

        // create spark configuration and context

        // todo: make master configurable for spark and pyspark streaming

        sparkConf = new SparkConf()
                .setMaster("local[*]")
                .setAppName(name);
        sparkContext = new JavaSparkContext(sparkConf);
        sqlContext = new SQLContext(sparkContext);

        sparkContext.addJar("/usr/local/spark/external/databricks/commons-csv-1.2.jar");
        sparkContext.addJar("/usr/local/spark/external/databricks/spark-csv_2.10-1.2.0.jar");
    }

    @Override
    protected String[] scriptFiles(){
        return new String[] { "python_init", "pyspark_init", "python_eval" };
    }

    @Override
    protected CommandLine getCommandLine(){

        String cmd = String.format("%s/%s",
                getProperties().getProperty("spark.home"),
                getProperties().getProperty("pyspark.command"));

        CommandLine commandLine = CommandLine.parse(cmd);
        commandLine.addArgument(this.getScriptPath(), false);
        commandLine.addArgument(Integer.toString(this.gatewayServer.getPort()), false);

        return commandLine;
    }

    protected void loadLogProperties(){
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("log4j.properties")){
            Properties props = new Properties();
            props.load(stream);
            PropertyConfigurator.configure(props);
        }
        catch(IOException ex){
            // do nothing
        }
    }

    public SparkConf getSparkConf(){ return this.sparkConf; }

    public JavaSparkContext getSparkContext(){ return this.sparkContext; }

    public SQLContext getSQLContext(){ return this.sqlContext; }

    public void describe(String schemaJson, List<String> names, List<List<Object>> stats){

        Map<String, Describe.DescribeFeature> features = new HashMap<>();

        DataFrameSchema schema = this.schemaFromJson(schemaJson);
        for(DataFrameSchema.DFSchemaField field : schema.getFields())
            features.put(field.getName(), new Describe.DescribeFeature(field.getName(), field.getType()));

        for (List<Object> stat : stats){

            String statName = stat.get(0).toString();
            for(int i = 1; i < stat.size(); i++){

                Describe.DescribeFeature feature = features.get(names.get(i));
                switch(statName){
                    case "count":
                        feature.setCount(Integer.parseInt(stat.get(i).toString()));
                        break;
                    case "max":
                        feature.setMax(Double.parseDouble(stat.get(i).toString()));
                        break;
                    case "mean":
                        feature.setAvg(Double.parseDouble(stat.get(i).toString()));
                        break;
                    case "min":
                        feature.setMin(Double.parseDouble(stat.get(i).toString()));
                        break;
                    case "stddev":
                        feature.setStdev(Double.parseDouble(stat.get(i).toString()));
                        break;
                }
            }
        }

        Describe describe = new Describe();
        for(DataFrameSchema.DFSchemaField field : schema.getFields())
            describe.add(features.get(field.getName()));

        this.notificationHandler.describe(describe);
    }

    public void collect(String schemaJson, List<String> names, List<List<Object>> data){

        Map<String, Feature> featureMap = new HashMap<>();

        DataFrameSchema schema = this.schemaFromJson(schemaJson);
        for(DataFrameSchema.DFSchemaField field : schema.getFields()){

            Feature feature;
            switch(field.getType()){
                case "double":
                    feature = new Feature<Double>(Double.class);
                    break;
                case "string":
                    feature = new Feature<String>(String.class);
                    break;
                default:
                    throw new InterpreterException(String.format("The specified data type, %s, is not supported.", field.getType()));
            }

            featureMap.put(field.getName(), feature);
        }

        for (List<Object> row : data){
            for(int i = 0; i < row.size(); i++){

                Feature feature = featureMap.get(names.get(i));
                feature.addObject(row.get(i));
            }
        }

        Features features = new Features();
        for(String name : names){
            features.add(featureMap.get(name));
        }

        this.notificationHandler.collect(features);
    }

    private DataFrameSchema schemaFromJson(String json){
        DataFrameSchema schema;
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            schema = objectMapper.readValue(json, DataFrameSchema.class);
        }
        catch(IOException ex){
            throw new InterpreterException(ex);
        }

        return schema;
    }
}
