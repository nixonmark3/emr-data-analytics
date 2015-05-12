package emr.analytics.service.messages;

import java.io.Serializable;
import java.util.UUID;

public class JobKillRequest implements Serializable {

    private UUID _id;

    public JobKillRequest(UUID id){

        _id = id;
    }

    public UUID getJobId(){ return _id; }
}
