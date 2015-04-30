package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobMode;

import java.util.UUID;

public abstract class JobStatus {

    private UUID _id;
    private JobMode _mode;

    public JobStatus(UUID id, JobMode mode){
        _id = id;
        _mode = mode;
    }

    public UUID getJobId(){
        return _id;
    }

    public JobMode getJobMode() { return _mode; }
}

