package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.io.Serializable;
import java.util.UUID;

public class TerminationRequest extends InputMessage implements Serializable {

    private UUID diagramId;
    private Mode mode;

    public TerminationRequest(UUID diagramId, Mode mode){
        super("terminationRequest");

        this.diagramId = diagramId;
        this.mode = mode;
    }

    public UUID getDiagramId(){ return this.diagramId; }

    public Mode getMode() { return this.mode; }
}
