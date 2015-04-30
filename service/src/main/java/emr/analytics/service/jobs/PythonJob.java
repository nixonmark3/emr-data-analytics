package emr.analytics.service.jobs;

import emr.analytics.service.jobs.AnalyticsJob;

import java.util.List;
import java.util.UUID;

public class PythonJob extends AnalyticsJob {

    public PythonJob(UUID id, JobMode mode, String diagramName, String fileName){
        super(id, mode, diagramName, fileName);
    }

    public PythonJob(UUID id, JobMode mode, String diagramName, String fileName, List<String> arguments){
        super(id, mode, diagramName, fileName, arguments);
    }

    @Override
    public List<String> processArguments(){

        ProcessArgumentBuilder argumentBuilder = new ProcessArgumentBuilder();

        // append file and arguments to the end of the list
        argumentBuilder.add(this._fileName);
        argumentBuilder.addAll(this._arguments);

        return argumentBuilder.get();
    }
}
