package emr.analytics.models.messages;

public class JobsSummary extends BaseMessage {

    private int offline;
    private int online;

    public JobsSummary(int offline, int online){
        super("jobs-summary");

        this.offline = offline;
        this.online = online;
    }

    public int getOffline() { return this.offline; }

    public int getOnline() { return this.online; }

}
