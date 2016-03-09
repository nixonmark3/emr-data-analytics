package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;
import java.util.UUID;

public class TaskTerminated extends TaskStatus {

    public TaskTerminated(UUID id, UUID sessionId, UUID diagramId, String diagramName, Mode mode){
        super(id, sessionId, diagramId, diagramName, mode, TaskStatusTypes.TERMINATED, "");
    }
}
