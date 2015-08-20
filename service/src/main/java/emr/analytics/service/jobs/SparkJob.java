package emr.analytics.service.jobs;

import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.JobRequest;

public class SparkJob extends AnalyticsJob {

    private TargetEnvironments targetEnvironment;
    private String metaData;

    public SparkJob(JobRequest request){
        super(request);

        this.targetEnvironment = request.getTargetEnvironment();
        this.metaData = request.getMetaData();
    }

    public TargetEnvironments getTargetEnvironment(){
        return this.targetEnvironment;
    }

    public String getMetaData() { return this.metaData; }
}
