package emr.analytics.service.messages;

import java.util.UUID;

public class ConsumeJob {

    private UUID diagramId;
    private ConsumeState state;

    public ConsumeJob(UUID diagramId, ConsumeState state){
        this.diagramId = diagramId;
        this.state = state;
    }

    public UUID getDiagramId(){ return this.diagramId; }

    public ConsumeState getState(){ return this.state; }

    public enum ConsumeState{
        START, END
    }
}
