package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public abstract class TaskStatus extends OutputMessage implements Serializable {

    private UUID diagramId;
    private String diagramName;
    private Mode mode;
    private TaskStatusTypes statusType;
    private Date dateTime;
    private String message;

    public TaskStatus(UUID taskId, UUID sessionId, UUID diagramId, String diagramName, Mode mode, TaskStatusTypes statusType, String message){
        super(taskId, sessionId, "task-status");

        this.dateTime = new Date();

        this.diagramId = diagramId;
        this.diagramName = diagramName;
        this.mode = mode;
        this.statusType = statusType;
        this.message = message;
    }

    public UUID getDiagramId() { return this.diagramId; }

    public String getDiagramName() { return this.diagramName; }

    public Mode getMode() { return this.mode; }

    public TaskStatusTypes getStatusType() { return this.statusType; }

    public Date getDateTime() { return this.dateTime; }

    public String getMessage() { return this.message; }

    public enum TaskStatusTypes {
        STARTED,
        COMPLETED,
        FAILED,
        BLOCK_STATE,
        DATA,
        ERROR,
        NOTIFICATION,
        OUTPUT,
        TERMINATED
    }
}
