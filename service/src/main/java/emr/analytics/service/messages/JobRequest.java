package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobMode;
import emr.analytics.service.jobs.LogLevel;
import emr.analytics.models.diagram.Diagram;

import java.util.UUID;

public class JobRequest {

    private UUID _jobId;
    private Diagram _diagram;
    private JobMode _mode;

    public JobRequest(JobMode mode, Diagram diagram){

        _jobId = UUID.randomUUID();
        _diagram = diagram;
        _mode = mode;
    }

    public UUID getJobId(){ return _jobId; }
    public Diagram getDiagram(){ return _diagram; }
    public JobMode getJobMode(){ return _mode; }
}