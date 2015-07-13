package emr.analytics.service.jobs;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.messages.JobRequest;

import java.io.Serializable;
import java.util.UUID;

public abstract class AnalyticsJob implements Serializable {

    protected UUID id;
    protected JobKey jobKey;
    protected String diagramName;
    protected String source;

    public AnalyticsJob(JobRequest request){
        this.id = UUID.randomUUID();
        this.jobKey = new JobKey(request.getDiagramId(), request.getMode());
        this.diagramName = request.getDiagramName();
        this.source = request.getSource();
    }

    public UUID getId() { return this.id; }

    public String getDiagramName(){ return this.diagramName; }

    public String getSource(){ return this.source; }

    public JobKey getKey() { return this.jobKey; }

    public Mode getMode() { return this.jobKey.getMode(); }
}