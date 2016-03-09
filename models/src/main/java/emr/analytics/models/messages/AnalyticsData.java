package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class AnalyticsData extends OutputMessage implements Serializable {

    private UUID diagramId;
    private List<Feature> features;

    public AnalyticsData(UUID taskId, UUID sessionId, UUID diagramId, Features features){
        this(taskId, sessionId);

        this.diagramId = diagramId;
        this.features = features.getFeatures();
    }

    public UUID getDiagramId() { return this.diagramId; }

    public List<Feature> getFeatures() { return this.features; }

    private AnalyticsData(UUID taskId, UUID sessionId) {
        super(taskId, sessionId, "data");
    }
}
