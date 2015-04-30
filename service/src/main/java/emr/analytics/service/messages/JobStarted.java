package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobMode;

import java.util.UUID;

public class JobStarted extends JobStatus {

    public JobStarted(UUID id, JobMode mode){ super(id, mode); }
}