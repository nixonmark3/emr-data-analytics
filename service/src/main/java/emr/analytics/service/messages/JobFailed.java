package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobMode;

import java.util.UUID;

public class JobFailed extends JobStatus {

    public JobFailed(UUID id, JobMode mode){ super(id, mode); }
}