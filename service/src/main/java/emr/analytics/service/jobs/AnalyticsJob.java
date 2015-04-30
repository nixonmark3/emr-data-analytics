package emr.analytics.service.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class AnalyticsJob implements Serializable {

    protected UUID _id;
    protected JobMode _mode;
    protected String _diagramName;
    protected String _fileName;
    protected List<String> _arguments;
    protected LogLevel _logLevel = LogLevel.Progress;

    public AnalyticsJob(UUID id, JobMode mode, String diagramName, String fileName){
        this(id, mode, diagramName, fileName, new ArrayList<String>());
    }

    public AnalyticsJob(UUID id, JobMode mode, String diagramName, String fileName, List<String> arguments){
        this._id = id;
        this._mode = mode;
        this._diagramName = diagramName;
        this._fileName = fileName;
        this._arguments = arguments;
    }

    public String getDiagramName(){
        return _diagramName;
    }

    public String getName(){ return _diagramName + "_" + _mode.toString(); }

    public void setLogLevel(LogLevel level){
        _logLevel = level;
    }

    public LogLevel getLogLevel(){ return _logLevel; }

    public UUID getId(){ return _id; }

    public String getFileName(){ return _fileName; }

    public JobMode getJobMode() { return _mode; }

    public abstract List<String> processArguments();

    protected class ProcessArgumentBuilder {

        List<String> arguments = new ArrayList<String>();

        public void add(String value){
            arguments.add(value);
        }

        public void addAll(List<String> values){
            arguments.addAll(values);
        }

        public void addKeyValue(String key, String value){
            arguments.add(key);
            arguments.add(value);
        }

        public void addOption(String key, Optional<String> value){
            if (value.isPresent()){
                arguments.add(key);
                arguments.add(value.get());
            }
        }

        public void addOptionList(String key, List<String> values) {
            if (!values.isEmpty()) {
                arguments.add(key);
                arguments.add(values.stream().reduce((x, y) -> x + "," + y).get());
            }
        }

        public List<String> get(){
            return arguments;
        }
    }
}
