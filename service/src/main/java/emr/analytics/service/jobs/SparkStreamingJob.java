package emr.analytics.service.jobs;

import emr.analytics.models.diagram.Diagram;

import java.util.UUID;

public class SparkStreamingJob extends AnalyticsJob {

    public SparkStreamingJob(UUID id, JobMode mode, Diagram diagram){
        super(id, mode, "sparkstreaming_driver.mustache", diagram);
    }
}
