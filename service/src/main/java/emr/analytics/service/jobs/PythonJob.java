package emr.analytics.service.jobs;

import emr.analytics.models.messages.JobRequest;

import java.util.ArrayList;
import java.util.List;

public class PythonJob extends ProcessJob {

    public PythonJob(JobRequest request){
        this(request, new ArrayList<String>());
    }

    public PythonJob(JobRequest request, List<String> arguments){

        super(request,
            "python3",                      // command
            "",                             // command prefix
            arguments);
    }

    @Override
    public List<String> processArguments(){

        ProcessArgumentBuilder argumentBuilder = new ProcessArgumentBuilder();

        // append file and arguments to the end of the list
        argumentBuilder.add(this.getFileName());
        argumentBuilder.addAll(this.commandArguments);

        return argumentBuilder.get();
    }
}
