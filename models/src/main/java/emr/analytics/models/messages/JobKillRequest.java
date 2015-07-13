package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.io.Serializable;
import java.util.UUID;

public class JobKillRequest extends BaseMessage implements Serializable {

    private UUID diagramId;
    private Mode mode;

    public JobKillRequest(UUID diagramId, Mode mode){
        super("jobKillRequest");

        this.diagramId = diagramId;
        this.mode = mode;
    }

    public UUID getDiagramId(){ return this.diagramId; }

    public Mode getMode() { return this.mode; }
}
