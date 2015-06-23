package emr.analytics.service.messages;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.Diagram;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class JobCompileRequest implements Serializable {

    private JobRequest request;

    public JobCompileRequest(JobRequest request){

        this.request = request;
    }

    public JobRequest getJobRequest(){ return this.request; }
}

