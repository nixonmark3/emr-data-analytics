package emr.analytics.service.messages;

import emr.analytics.models.definition.Mode;

import java.io.Serializable;
import java.util.UUID;

public abstract class JobStatus implements Serializable {

    private UUID _id;
    private Mode _mode;

    public JobStatus(UUID id, Mode mode){
        _id = id;
        _mode = mode;
    }

    public UUID getJobId(){
        return _id;
    }


    public Mode getMode() { return _mode; }
}

