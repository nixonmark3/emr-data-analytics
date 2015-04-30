package emr.analytics.service.messages;

import emr.analytics.service.jobs.JobMode;

import java.util.UUID;

public class JobStopped extends JobStatus {

    public JobStopped(UUID id, JobMode mode){ super(id, mode); }
}
