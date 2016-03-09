package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.UUID;

public class AnalyticsDescribe extends OutputMessage implements Serializable {

    private UUID diagramId;
    private Describe describe;

    public AnalyticsDescribe(UUID taskId, UUID sessionId, UUID diagramId, Describe describe){
        this(taskId, sessionId);

        this.diagramId = diagramId;
        this.describe = describe;
    }

    public UUID getDiagramId() { return this.diagramId; }

    public Describe getDescribe() { return this.describe; }

    private AnalyticsDescribe(UUID taskId, UUID sessionId) {
        super(taskId, sessionId, "describe");
    }
}
