package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.UUID;

public class AnalyticsData extends OutputMessage implements Serializable {

    private UUID diagramId;
    private Features features;

    public AnalyticsData(UUID diagramId, Features features){
        this();

        this.diagramId = diagramId;
        this.features = features;
    }

    public UUID getDiagramId() { return this.diagramId; }

    public Features getFeatures() { return this.features; }

    private AnalyticsData() {
        super("data");
    }
}
