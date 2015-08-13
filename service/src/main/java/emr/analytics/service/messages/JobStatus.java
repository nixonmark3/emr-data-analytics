package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobKey;

import java.io.Serializable;
import java.util.UUID;

public class JobStatus implements Serializable {

    private JobStatusTypes statusType;

    public JobStatus(JobStatusTypes statusType){
        this.statusType = statusType;
    }

    public JobStatusTypes getStatusType() { return this.statusType; }
}

