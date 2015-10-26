package emr.analytics.models.messages;

public class PingResponse extends OutputMessage {
    private boolean value;

    public PingResponse(){ this(false); }

    public PingResponse(boolean value) {
        super("ping");
        this.value = value;
    }

    public boolean getValue() { return this.value; }

    public void setValue(boolean value){ this.value = value; }
}
