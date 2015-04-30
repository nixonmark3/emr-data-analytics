package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobMode;

import java.util.UUID;

public class JobCompleted extends JobStatus {

    public JobCompleted(UUID id, JobMode mode){ super(id, mode); }
}