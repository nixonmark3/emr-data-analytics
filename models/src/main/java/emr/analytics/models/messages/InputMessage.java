package emr.analytics.models.messages;

import java.io.Serializable;

public class InputMessage implements Serializable {
    private String messageType;

    public InputMessage(String messageType){
        this.messageType = messageType;
    }

    public String getMessageType(){ return this.messageType; }
}