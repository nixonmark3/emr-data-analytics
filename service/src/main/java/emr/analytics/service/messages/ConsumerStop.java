package emr.analytics.service.messages;

import java.util.UUID;

public class ConsumerStop {

    private UUID diagramId;

    public ConsumerStop(UUID diagramId) {
        this.diagramId = diagramId;
    }

    public UUID getDiagramId() { return this.diagramId; }
}
