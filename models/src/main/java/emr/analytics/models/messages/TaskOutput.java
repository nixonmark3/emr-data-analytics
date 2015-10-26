package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class TaskOutput extends TaskStatus {

    public TaskOutput(UUID diagramId, String diagramName, Mode mode, String message){
        super(diagramId, diagramName, mode, TaskStatusTypes.OUTPUT, message);
    }
}
