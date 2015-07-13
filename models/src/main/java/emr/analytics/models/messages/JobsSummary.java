package emr.analytics.models.messages;

public class JobsSummary {

    private int offline;
    private int online;

    public JobsSummary(int offline, int online){

        this.offline = offline;
        this.online = online;
    }

    public int getOffline() { return this.offline; }

    public int getOnline() { return this.online; }

}
