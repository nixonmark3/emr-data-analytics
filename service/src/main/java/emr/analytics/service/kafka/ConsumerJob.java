package emr.analytics.service.kafka;

import emr.analytics.models.messages.JobVariable;

public class ConsumerJob {

    private JobVariable jobVariable;
    private String jobMetaData;

    public ConsumerJob(JobVariable jobVariable, String jobMetaData) {

        this.jobVariable = jobVariable;
        this.jobMetaData = jobMetaData;
    }

    public JobVariable getJobVariable() { return this.jobVariable; }

    public String getJobMetaData() { return this.jobMetaData; }
}
