package emr.analytics.service.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SparkJob extends AnalyticsJob {

    private Optional<String> _className = Optional.empty();
    private Optional<String> _master = Optional.empty();
    private Optional<String> _driverMemory = Optional.empty();
    private List<String> _jars = new ArrayList<String>();
    private List<String> _pyFiles = new ArrayList<String>();

    public SparkJob(UUID id, JobMode mode, String diagramName, String fileName){
        super(id, mode, diagramName, fileName);
    }

    public SparkJob(UUID id, JobMode mode, String diagramName, String fileName, List<String> arguments){ super(id, mode, diagramName, fileName, arguments); }

    public SparkJob setClass(String value){
        _className = Optional.of(value);
        return this;
    }

    public SparkJob setMaster(String value){
        _master = Optional.of(value);
        return this;
    }

    public SparkJob setDriverMemory(String value){
        _driverMemory = Optional.of(value);
        return this;
    }

    public SparkJob addJarFile(String value){
        _jars.add(value);
        return this;
    }

    public SparkJob addJarFiles(List<String> values){
        _jars.addAll(values);
        return this;
    }

    public SparkJob addPythonFile(String value){
        _pyFiles.add(value);
        return this;
    }

    public SparkJob addPythonFiles(List<String> values){
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
        argumentBuilder.add(this._fileName);
        argumentBuilder.addAll(this._arguments);

        return argumentBuilder.get();
    }
}
