package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StreamingTasks extends OutputMessage implements Serializable {

    private List<StreamingTask> items;

    public StreamingTasks(UUID sessionId){
        super(UUID.randomUUID(), sessionId, "streaming-tasks");

        this.items = new ArrayList<>();
    }

    public List<StreamingTask> getItems() {
        return this.items;
    }

    public void add(StreamingTask item){
        items.add(item);
    }
}