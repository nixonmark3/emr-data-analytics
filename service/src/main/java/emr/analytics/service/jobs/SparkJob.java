package emr.analytics.service.jobs;

import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.JobRequest;

public class SparkJob extends AnalyticsJob {

    private TargetEnvironments targetEnvironment;

    public SparkJob(JobRequest request){
        super(request);

        targetEnvironment = request.getTargetEnvironment();
    }

    public TargetEnvironments getTargetEnvironment(){
        return this.targetEnvironment;
    }
}
