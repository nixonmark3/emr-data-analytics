package emr.analytics.models.messages;

import java.util.UUID;

public class TaskCounts extends OutputMessage {

    private int offline;
    private int online;
    private int streaming;

    public TaskCounts(){
        this(0, 0, 0);
    }

    public TaskCounts(int offline, int online, int streaming){
        super(null, null, "task-counts");

        this.offline = offline;
        this.online = online;
        this.streaming = streaming;
    }

    public int getOffline() { return this.offline; }

    public int getOnline() { return this.online; }

    public int getStreaming() { return this.streaming; }

}
