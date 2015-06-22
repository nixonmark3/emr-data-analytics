package emr.analytics.service.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class JobCompleted extends JobStatus {

    public JobCompleted(UUID id, Mode mode){ super(id, mode); }
}