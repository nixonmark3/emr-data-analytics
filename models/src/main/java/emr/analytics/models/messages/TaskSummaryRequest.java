package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.UUID;

public class TaskSummaryRequest extends InputMessage implements Serializable {

    private UUID diagramId;

    public TaskSummaryRequest(UUID diagramId){
        super("task-summary-request");

        this.diagramId = diagramId;
    }

    public UUID getDiagramId(){ return this.diagramId; }
}