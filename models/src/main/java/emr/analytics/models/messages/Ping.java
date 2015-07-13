package emr.analytics.models.messages;

public class Ping extends BaseMessage {
    private boolean value;

    public Ping(){ this(false); }

    public Ping(boolean value) {
        super("ping");
        this.value = value;
    }

    public boolean getValue() { return this.value; }

    public void setValue(boolean value){ this.value = value; }
}
