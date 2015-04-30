package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobMode;

import java.util.UUID;

public class JobProgress extends JobStatus {

    private String _progressMessage;

    public JobProgress(UUID id, JobMode mode, String progressMessage){
        super(id, mode);
        _progressMessage = progressMessage;
    }

    public String getProgressMessage(){
        return _progressMessage;
    }
}

