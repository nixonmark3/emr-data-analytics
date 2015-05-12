package emr.analytics.service.jobs;

import emr.analytics.models.diagram.Diagram;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PythonJob extends ProcessJob {

    public PythonJob(UUID id, JobMode mode, Diagram diagram){
        this(id, mode, diagram, new ArrayList<String>());
    }

    public PythonJob(UUID id, JobMode mode, Diagram diagram, List<String> arguments){

        super(id,
            mode,
            "python_driver.mustache",       // template
            "python3",                      // command
            "",                             // command prefix
            diagram,
            arguments);
    }

    @Override
    public List<String> processArguments(){

        ProcessArgumentBuilder argumentBuilder = new ProcessArgumentBuilder();

        // append file and arguments to the end of the list
        argumentBuilder.add(this.getFileName());
        argumentBuilder.addAll(this._commandArguments);

        return argumentBuilder.get();
    }
}
