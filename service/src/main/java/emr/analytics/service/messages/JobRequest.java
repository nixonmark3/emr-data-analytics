package emr.analytics.service.messages;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.Diagram;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class JobRequest implements Serializable {

    private UUID _jobId;
    private Diagram _diagram;
    private HashMap<String, String> _models;

    public JobRequest(Diagram diagram, HashMap models){

        _jobId = UUID.randomUUID();
        _diagram = diagram;
        _models = models;
    }

    public UUID getJobId(){ return _jobId; }
    public Diagram getDiagram(){ return _diagram; }
    public Mode getMode(){ return _diagram.getMode(); }
    public HashMap<String, String> getModels(){ return _models; }
}

