package models.project;

import emr.analytics.models.messages.BaseMessage;
import emr.analytics.models.messages.JobInfo;

public class DeploymentStatus extends BaseMessage {

    private JobInfo jobInfo;

    public DeploymentStatus(JobInfo jobInfo){
        super("deploymentStatus");

        this.jobInfo = jobInfo;
    }

    public JobInfo getJobInfo() { return this.jobInfo; }
}
