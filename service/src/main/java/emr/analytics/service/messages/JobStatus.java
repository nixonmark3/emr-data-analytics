package emr.analytics.service.messages;

import java.util.UUID;

public abstract class JobStatus {

    private UUID _id;

    public JobStatus(UUID id){
        _id = id;
    }

    public UUID getJobId(){
        return _id;
    }
}

