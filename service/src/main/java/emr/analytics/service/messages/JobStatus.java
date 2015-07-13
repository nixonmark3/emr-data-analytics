package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobKey;

import java.io.Serializable;
import java.util.UUID;

public class JobStatus implements Serializable {

    private UUID jobId;
    private JobStatusTypes statusType;

    public JobStatus(UUID jobId, JobStatusTypes statusType){
        this.jobId = jobId;
        this.statusType = statusType;
    }

    public UUID getJobId(){ return this.jobId; }

    public JobStatusTypes getStatusType() { return this.statusType; }
}

