package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StreamingTasks extends OutputMessage implements Serializable {

    private List<StreamingTask> items;

    public StreamingTasks(){
        super("streaming-tasks");

        this.items = new ArrayList<>();
    }

    public List<StreamingTask> getItems() {
        return this.items;
    }

    public void add(StreamingTask item){
        items.add(item);
    }
}
