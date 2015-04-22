package emr.analytics.models.messages;

import emr.analytics.models.diagram.Diagram;

import java.io.Serializable;
import java.util.UUID;

/*
 * Message for evaluation request
 */
public class EvaluationRequest implements Serializable {
    private UUID jobId = null;
    private Diagram diagram = null;
    private boolean subscribe = true;

    private EvaluationRequest() {}

    public EvaluationRequest(UUID jobId, Diagram diagram) {
        this.jobId = jobId;
        this.diagram = diagram;
    }

    public UUID getJobId() { return jobId; }

    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public Diagram getDiagram() { return diagram; }

    public void setDiagram(Diagram diagram) { this.diagram = diagram; }

    public boolean getSubscribe(){ return this.subscribe; }

    public void setSubscribe(boolean subscribe) { this.subscribe = subscribe; }
}
