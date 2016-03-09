package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class TaskData extends TaskStatus {

    public TaskData(UUID id, UUID sessionId, UUID diagramId, String diagramName, Mode mode, String data){
        super(id, sessionId, diagramId, diagramName, mode, TaskStatusTypes.DATA, data);
    }
}
