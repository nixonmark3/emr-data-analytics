package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobKey;

import java.util.UUID;

public class JobFailed extends JobStatus {

    private String message;

    public JobFailed(String message){
        super(JobStatusTypes.FAILED);

        this.message = message;
    }

    public String getMessage(){ return this.message; }
}
