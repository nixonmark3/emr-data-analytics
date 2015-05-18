package emr.analytics.service.jobs;

import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.service.messages.JobRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PythonJob extends ProcessJob {

    public PythonJob(JobRequest request, HashMap<String, Definition> definitions){
        this(request, definitions, new ArrayList<String>());
    }

    public PythonJob(JobRequest request, HashMap<String, Definition> definitions, List<String> arguments){

        super(request,
            "python_driver.mustache",       // template
            "python3",                      // command
            "",                             // command prefix
            definitions,
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
