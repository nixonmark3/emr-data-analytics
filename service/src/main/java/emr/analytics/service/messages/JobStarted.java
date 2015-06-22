package emr.analytics.service.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class JobStarted extends JobStatus {

    public JobStarted(UUID id, Mode mode){ super(id, mode); }
}