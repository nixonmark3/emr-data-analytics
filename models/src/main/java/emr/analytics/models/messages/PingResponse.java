package emr.analytics.models.messages;

import java.util.UUID;

public class PingResponse extends OutputMessage {
    private boolean value;

    public PingResponse(UUID id){ this(id, false); }

    public PingResponse(UUID id, boolean value) {
        super(id, null, "ping");
        this.value = value;
    }

    public boolean getValue() { return this.value; }

    public void setValue(boolean value){ this.value = value; }
}
