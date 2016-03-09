package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class TaskFailed extends TaskStatus {

    public TaskFailed(UUID id, UUID sessionId, UUID diagramId, String diagramName, Mode mode, String message){
        super(id, sessionId, diagramId, diagramName, mode, TaskStatusTypes.FAILED, message);
    }
}
