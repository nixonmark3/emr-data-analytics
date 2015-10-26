package models;

import emr.analytics.models.messages.AnalyticsTask;
import emr.analytics.models.messages.OutputMessage;

public class DeploymentStatus extends OutputMessage {

    private AnalyticsTask task;

    public DeploymentStatus(AnalyticsTask task){
        super("deployment-status");

        this.task = task;
    }

    public AnalyticsTask getTask() { return this.task; }
}
