package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class TaskFailed extends TaskStatus {

    public TaskFailed(UUID diagramId, String diagramName, Mode mode, String message){
        super(diagramId, diagramName, mode, TaskStatusTypes.FAILED, message);
    }
}
