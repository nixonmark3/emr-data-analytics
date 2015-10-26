package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class TaskError extends TaskStatus {

    public TaskError(UUID diagramId, String diagramName, Mode mode, String message){
        super(diagramId, diagramName, mode, TaskStatusTypes.ERROR, message);
    }
}
