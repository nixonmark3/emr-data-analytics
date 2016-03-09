package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.UUID;

public class OutputMessage implements Serializable {
    private UUID id;
    private UUID sessionId;
    private String messageType;

    public OutputMessage(UUID id, UUID sessionId, String messageType){
        this.id = id;
        this.sessionId = sessionId;
        this.messageType = messageType;
    }

    public UUID getId() { return this.id; }

    public UUID getSessionId() { return this.sessionId; }

    public String getMessageType(){ return this.messageType; }
}