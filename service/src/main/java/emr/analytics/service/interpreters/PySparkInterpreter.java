package emr.analytics.service.interpreters;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.log4j.PropertyConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PySparkInterpreter extends PythonInterpreter implements ExecuteResultHandler {

    protected SparkConf sparkConf;
    protected JavaSparkContext sparkContext;

    public PySparkInterpreter(String name, InterpreterNotificationHandler notificationHandler){
        super(notificationHandler);

        // load spark and pySpark properties
        loadProperties("spark");
        loadProperties("pySpark");
        // load py4j properties
        loadLogProperties();

        // create spark configuration and context
        sparkConf = new SparkConf()
                .setMaster("local[2]")
                .setAppName(name);
        sparkContext = new JavaSparkContext(sparkConf);
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
}
