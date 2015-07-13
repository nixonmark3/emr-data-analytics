package emr.analytics.models.messages;

import java.io.Serializable;

public class BaseMessage implements Serializable {
    private String type;

    public BaseMessage(String type){
        this.type = type;
    }

    public String getType(){ return this.type; }
}