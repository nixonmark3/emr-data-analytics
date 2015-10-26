package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class TaskStarted extends TaskStatus {

    public TaskStarted(UUID diagramId, String diagramName, Mode mode, String source){
        super(diagramId, diagramName, mode, TaskStatusTypes.STARTED, source);
    }
}
