package emr.analytics.service.jobs;

import emr.analytics.models.messages.JobRequest;

import java.util.*;

public class PySparkJob extends ProcessJob {

    private Optional<String> className = Optional.empty();
    private Optional<String> master = Optional.empty();
    private Optional<String> driverMemory = Optional.empty();
    private List<String> jars = new ArrayList<String>();
    private List<String> pyFiles = new ArrayList<String>();

    public PySparkJob(JobRequest request){
        this(request, new ArrayList<String>());
    }

    public PySparkJob(JobRequest request, List<String> arguments){

        super(request,
            "bin/spark-submit",             // command
            "SPARK_HOME",                   // command prefix
            arguments);
    }

    public PySparkJob setClass(String value){
        className = Optional.of(value);
        return this;
    }

    public PySparkJob setMaster(String value){
        master = Optional.of(value);
        return this;
    }

    public PySparkJob setDriverMemory(String value){
        driverMemory = Optional.of(value);
        return this;
    }

    public PySparkJob addJarFile(String value){
        jars.add(value);
        return this;
    }

    public PySparkJob addJarFiles(List<String> values){
        jars.addAll(values);
        return this;
    }

    public PySparkJob addPythonFile(String value){
        pyFiles.add(value);
        return this;
    }

    public PySparkJob addPythonFiles(List<String> values){
        pyFiles.addAll(values);
        return this;
    }

    @Override
    public List<String> processArguments(){

        ProcessArgumentBuilder argumentBuilder = new ProcessArgumentBuilder();

        // begin by appending the spark job name
        argumentBuilder.addKeyValue("--name", this.id.toString());

        // add optional and list variables
        argumentBuilder.addOption("--class", className);
        argumentBuilder.addOption("--master", master);
        argumentBuilder.addOption("--driver-memory", driverMemory);
        argumentBuilder.addOptionList("--jars", jars);
        argumentBuilder.addOptionList("--py-files", pyFiles);

        // append file and arguments to the end of the list
        argumentBuilder.add(this.getFileName());
        argumentBuilder.addAll(this.commandArguments);

        return argumentBuilder.get();
    }
}
