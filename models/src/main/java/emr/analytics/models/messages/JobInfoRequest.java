package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.UUID;

public class JobInfoRequest extends BaseMessage implements Serializable {

    private UUID diagramId;

    public JobInfoRequest(UUID diagramId){
        super("jobInfoRequest");

        this.diagramId = diagramId;
    }

    public UUID getDiagramId(){ return this.diagramId; }
}