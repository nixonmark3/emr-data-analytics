package emr.analytics.service.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class JobStopped extends JobStatus {

    public JobStopped(UUID id, Mode mode){ super(id, mode); }
}
