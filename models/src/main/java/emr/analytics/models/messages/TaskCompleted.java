package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class TaskCompleted extends TaskStatus {

    public TaskCompleted(UUID diagramId, String diagramName, Mode mode){
        super(diagramId, diagramName, mode, TaskStatusTypes.COMPLETED, "");
    }
}
