package emr.analytics.service.messages;

import java.util.UUID;

public class JobProgress extends JobStatus {

    private String _progressMessage;

    public JobProgress(UUID id, String progressMessage){
        super(id);
        _progressMessage = progressMessage;
    }

    public String getProgressMessage(){
        return _progressMessage;
    }
}

