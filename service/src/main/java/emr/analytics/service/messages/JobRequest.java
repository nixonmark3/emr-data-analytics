package emr.analytics.service.messages;

import emr.analytics.service.jobs.LogLevel;
import emr.analytics.service.jobs.TargetEnvironments;
import emr.analytics.models.diagram.Diagram;

import java.util.UUID;

public class JobRequest {

    private UUID _jobId;
    private LogLevel _logLevel;
    private Diagram _diagram;
    private TargetEnvironments _targetEnvironment;

    public JobRequest(LogLevel logLevel,
        Diagram diagram,
        TargetEnvironments targetEnvironment){

        _jobId = UUID.randomUUID();
        _logLevel = logLevel;
        _diagram = diagram;
        _targetEnvironment = targetEnvironment;
    }

    public UUID getJobId(){ return _jobId; }
    public LogLevel getLogLevel(){ return _logLevel; }
    public Diagram getDiagram(){ return _diagram; }
    public TargetEnvironments getTargetEnvironments(){ return _targetEnvironment; }
}