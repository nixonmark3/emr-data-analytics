package emr.analytics.service.messages;

import emr.analytics.models.messages.Consumers;

import java.util.UUID;

public class ConsumerStart {

    private UUID id;
    private UUID diagramId;
    private Consumers consumers;

    public ConsumerStart(UUID id, UUID diagramId, Consumers consumers) {
        this.id = id;
        this.diagramId = diagramId;
        this.consumers = consumers;
    }

    public UUID getId() { return this.id; }

    public UUID getDiagramId() { return this.diagramId; }

    public Consumers getConsumers() { return this.consumers; }
}
