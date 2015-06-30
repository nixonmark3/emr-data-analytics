package emr.analytics.models.diagram;

import java.io.Serializable;

public class DiagramContainer implements Serializable {

    private Diagram offline = null;
    private Diagram online = null;

    private DiagramContainer() {}

    public Diagram getOffline() {
        return offline;
    }

    public void setOffline(Diagram offline) {
        this.offline = offline;
    }

    public Diagram getOnline() {
        return online;
    }

    public void setOnline(Diagram online) {
        this.online = online;
    }
}
