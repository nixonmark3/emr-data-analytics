package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobMode;
import emr.analytics.service.jobs.LogLevel;
import emr.analytics.models.diagram.Diagram;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class JobRequest implements Serializable {

    private UUID _jobId;
    private Diagram _diagram;
    private JobMode _mode;
    private HashMap<String, String> _models;

    public JobRequest(JobMode mode, Diagram diagram, HashMap models){

        _jobId = UUID.randomUUID();
        _diagram = diagram;
        _mode = mode;
        _models = models;
    }

    public UUID getJobId(){ return _jobId; }
    public Diagram getDiagram(){ return _diagram; }
    public JobMode getJobMode(){ return _mode; }
    public HashMap<String, String> getModels(){ return _models; }
}

