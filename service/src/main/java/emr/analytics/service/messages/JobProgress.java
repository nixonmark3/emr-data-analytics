package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobKey;

import java.util.UUID;

public class JobProgress extends JobStatus {

    private String progressKey;
    private String progressValue;

    public JobProgress(String progressKey, String progressValue){
        super(JobStatusTypes.PROGRESS);

        this.progressKey = progressKey;
        this.progressValue = progressValue;
    }

    public String getProgressKey(){ return this.progressKey; }

    public String getProgressValue(){
        return this.progressValue;
    }
}

