package emr.analytics.service.interpreters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.messages.Describe;
import emr.analytics.models.messages.Feature;
import emr.analytics.models.messages.Features;
import emr.analytics.service.models.RawStatistics;
import emr.analytics.service.models.Schema;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.log4j.PropertyConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PySparkInterpreter extends PythonInterpreter implements ExecuteResultHandler {

    protected SparkConf sparkConf;
    protected JavaSparkContext sparkContext;
    protected SQLContext sqlContext;

    public PySparkInterpreter(String name, InterpreterNotificationHandler notificationHandler, Properties properties){
        super(notificationHandler, properties);

        loadLogProperties();

        // create spark configuration and context

        // todo: make master configurable for spark and pyspark streaming

        sparkConf = new SparkConf()
                .setMaster("local[*]")
                .setAppName(name);
        sparkContext = new JavaSparkContext(sparkConf);
        sqlContext = new SQLContext(sparkContext);

        this.sparkContext.addFile(String.format("%s/PySparkCSV.py", this.getWorkingDirectory().getAbsolutePath()));
    }

    @Override
    protected String[] scriptFiles(){
        return new String[] { "python_init", "pyspark_init", "pyspark_methods", "python_eval" };
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

    @Override
    protected File getWorkingDirectory(){
        return new File(this.getProperties().getProperty("pyspark.workingDir"));
    }

    public void describe(String name, String schema, String stats){

        ObjectMapper objectMapper = new ObjectMapper();

        List<Schema> dataSchema;
        try{
            TypeReference<List<Schema>> typeReference
                    = new TypeReference<List<Schema>>() {};

            dataSchema = objectMapper.readValue(schema, typeReference);
        }
        catch(IOException ex){
            throw new InterpreterException(ex);
        }

        RawStatistics<String> rawStatistics;
        try{
            TypeReference<RawStatistics<String>> typeReference
                    = new TypeReference<RawStatistics<String>>() {};

            rawStatistics = objectMapper.readValue(stats, typeReference);
        }
        catch(IOException ex){
            throw new InterpreterException(ex);
        }

        Map<String, Describe.DescribeFeature> features = new HashMap<>();

        for(Schema entry : dataSchema)
            features.put(entry.getName(), new Describe.DescribeFeature(entry.getName(), entry.getType(), name));

        for(int row = 0; row < rawStatistics.getData().size(); row++){

            List<String> stat = rawStatistics.getData().get(row);
            String statName = stat.get(0);
            for(int col = 1; col < stat.size(); col++){

                Describe.DescribeFeature feature = features.get(rawStatistics.getColumns().get(col));
                switch(statName){
                    case "count":
                        feature.setCount(Integer.parseInt(stat.get(col)));
                        break;
                    case "max":
                        feature.setMax(Double.parseDouble(stat.get(col)));
                        break;
                    case "mean":
                        feature.setAvg(Double.parseDouble(stat.get(col)));
                        break;
                    case "min":
                        feature.setMin(Double.parseDouble(stat.get(col)));
                        break;
                    case "stddev":
                        feature.setStdev(Double.parseDouble(stat.get(col)));
                        break;
                }
            }
        }

        Describe describe = new Describe();
        for(Schema entry : dataSchema)
            describe.add(features.get(entry.getName()));

        this.notificationHandler.describe(describe);
    }

    public void collect(String schema, String data){

        ObjectMapper objectMapper = new ObjectMapper();

        List<Schema> dataSchema;
        try{
            TypeReference<List<Schema>> typeReference
                    = new TypeReference<List<Schema>>() {};

            dataSchema = objectMapper.readValue(schema, typeReference);
        }
        catch(IOException ex){
            throw new InterpreterException(ex);
        }

        List<List<Object>> rawData;
        try{
            TypeReference<List<List<Object>>> typeReference
                    = new TypeReference<List<List<Object>>>() {};

            rawData = objectMapper.readValue(data, typeReference);
        }
        catch(IOException ex){
            throw new InterpreterException(ex);
        }

        Features features = new Features();
        for(Schema entry : dataSchema){

            String name = entry.getName();
            String dataType = entry.getType();

            Feature feature;
            switch(dataType){
                case "timestamp":
                    feature = new Feature<Date>(name, Date.class);
                    break;
                case "double":
                    feature = new Feature<Double>(name, Double.class);
                    break;
                case "string":
                    feature = new Feature<String>(name, String.class);
                    break;
                default:
                    throw new InterpreterException(String.format("The specified data type, %s, is not supported.", dataType));
            }

            features.add(feature);
        }

        for (List<Object> row : rawData){
            for(int i = 0; i < row.size(); i++){
                Feature feature = features.getFeature(i);
                feature.addObject(row.get(i));
            }
        }

        this.notificationHandler.collect(features);
    }
}
