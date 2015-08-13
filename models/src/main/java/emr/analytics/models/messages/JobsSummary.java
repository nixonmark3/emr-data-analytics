package emr.analytics.models.messages;

public class JobsSummary extends BaseMessage {

    private int offline;
    private int online;
    private int streaming;

    public JobsSummary(){
        this(0, 0, 0);
    }

    public JobsSummary(int offline, int online, int streaming){
        super("jobs-summary");

        this.offline = offline;
        this.online = online;
        this.streaming = streaming;
    }

    public int getOffline() { return this.offline; }

    public int getOnline() { return this.online; }

    public int getStreaming() { return this.streaming; }

}
