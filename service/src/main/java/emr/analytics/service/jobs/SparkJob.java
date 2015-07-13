package emr.analytics.service.jobs;

import emr.analytics.models.messages.JobRequest;

public class SparkJob extends AnalyticsJob {

    public SparkJob(JobRequest request){
        super(request);
    }
}
