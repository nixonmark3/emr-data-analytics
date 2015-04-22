package emr.analytics.service.messages;

import java.util.UUID;

public class JobStarted extends JobStatus {

    public JobStarted(UUID id){ super(id); }
}