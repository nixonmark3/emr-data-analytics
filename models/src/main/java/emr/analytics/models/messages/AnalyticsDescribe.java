package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.UUID;

public class AnalyticsDescribe extends OutputMessage implements Serializable {

    private UUID diagramId;
    private Describe describe;

    public AnalyticsDescribe(UUID diagramId, Describe describe){
        this();

        this.diagramId = diagramId;
        this.describe = describe;
    }

    public UUID getDiagramId() { return this.diagramId; }

    public Describe getDescribe() { return this.describe; }

    private AnalyticsDescribe() {
        super("describe");
    }
}