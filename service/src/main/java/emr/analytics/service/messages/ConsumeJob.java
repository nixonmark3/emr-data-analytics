package emr.analytics.service.messages;

import java.util.UUID;

public class ConsumeJob {

    private UUID diagramId;
    private ConsumeState state;
    private String metaData;

    public ConsumeJob(UUID diagramId, String metaData, ConsumeState state) {

        this.diagramId = diagramId;
        this.state = state;
        this.metaData = metaData;
    }

    public UUID getDiagramId() { return this.diagramId; }

    public String getMetaData() { return this.metaData; }

    public ConsumeState getState() { return this.state; }

    public enum ConsumeState {

        START,
        END
    }
}
