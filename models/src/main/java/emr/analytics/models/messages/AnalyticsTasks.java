package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnalyticsTasks extends OutputMessage implements Serializable {

    private List<AnalyticsTask> tasks;

    public AnalyticsTasks(UUID sessionId){
        super(UUID.randomUUID(), sessionId, "analytics-tasks");

        this.tasks = new ArrayList<AnalyticsTask>();
    }

    public List<AnalyticsTask> getTasks() {
        return this.tasks;
    }

    public void add(AnalyticsTask task){
        tasks.add(task);
    }
}
