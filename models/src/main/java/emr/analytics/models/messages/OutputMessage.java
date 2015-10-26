package emr.analytics.models.messages;

import java.io.Serializable;

public class OutputMessage implements Serializable {
    private String messageType;

    public OutputMessage(String messageType){
        this.messageType = messageType;
    }

    public String getMessageType(){ return this.messageType; }
}