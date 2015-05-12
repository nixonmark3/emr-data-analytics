package emr.analytics.service.jobs;

import emr.analytics.models.diagram.Diagram;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PySparkJob extends ProcessJob {

    private Optional<String> _className = Optional.empty();
    private Optional<String> _master = Optional.empty();
    private Optional<String> _driverMemory = Optional.empty();
    private List<String> _jars = new ArrayList<String>();
    private List<String> _pyFiles = new ArrayList<String>();

    public PySparkJob(UUID id, JobMode mode, Diagram diagram){
        this(id, mode, diagram, new ArrayList<String>());
    }

    public PySparkJob(UUID id, JobMode mode, Diagram diagram, List<String> arguments){

        super(id,
            mode,
            "pyspark_driver.mustache",      // template
            "bin/spark-submit",             // command
            "SPARK_HOME",                   // command prefix
            diagram,
            arguments);
    }

    public PySparkJob setClass(String value){
        _className = Optional.of(value);
        return this;
    }

    public PySparkJob setMaster(String value){
        _master = Optional.of(value);
        return this;
    }

    public PySparkJob setDriverMemory(String value){
        _driverMemory = Optional.of(value);
        return this;
    }

    public PySparkJob addJarFile(String value){
        _jars.add(value);
        return this;
    }

    public PySparkJob addJarFiles(List<String> values){
        _jars.addAll(values);
        return this;
    }

    public PySparkJob addPythonFile(String value){
        _pyFiles.add(value);
        return this;
    }

    public PySparkJob addPythonFiles(List<String> values){
        _pyFiles.addAll(values);
        return this;
    }

    @Override
    public List<String> processArguments(){

        ProcessArgumentBuilder argumentBuilder = new ProcessArgumentBuilder();

        // begin by appending the spark job name
        argumentBuilder.addKeyValue("--name", this._id.toString());

        // add optional and list variables
        argumentBuilder.addOption("--class", _className);
        argumentBuilder.addOption("--master", _master);
        argumentBuilder.addOption("--driver-memory", _driverMemory);
        argumentBuilder.addOptionList("--jars", _jars);
        argumentBuilder.addOptionList("--py-files", _pyFiles);

        // append file and arguments to the end of the list
        argumentBuilder.add(this.getFileName());
        argumentBuilder.addAll(this._commandArguments);

        return argumentBuilder.get();
    }
}
