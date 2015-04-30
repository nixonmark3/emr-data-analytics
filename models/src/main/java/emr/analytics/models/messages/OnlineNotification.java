package emr.analytics.models.messages;

import java.util.UUID;

public class OnlineNotification {

    private UUID id = null;
    private int state = 0;
    private String message = "";

    private OnlineNotification() {}

    public OnlineNotification(UUID id, int state, String message){
        this.id = id;
        this.state = state;
        this.message = message;
    }

    public UUID getId(){
        return this.id;
    }

    public int getState(){
        return this.state;
    }

    public String getMessage(){
        return this.message;
    }
}
