package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class TaskNotification extends TaskStatus {

    private String key;

    public TaskNotification(UUID diagramId, String diagramName, Mode mode, String key, String message){
        super(diagramId, diagramName, mode, TaskStatusTypes.NOTIFICATION, message);

        this.key = key;
    }

    public String getKey(){
        return this.key;
    }
}
