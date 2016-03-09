package emr.analytics.models.messages;

import emr.analytics.models.structures.CircularQueue;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class TaskVariable extends OutputMessage implements Serializable {
    private UUID diagramId;
    private String key;
    private CircularQueue<String> queue;

    public TaskVariable(UUID id, UUID diagramId, String key, int capacity){
        super(id, null, "task-variable");

        this.diagramId = diagramId;
        this.key = key;
        this.queue = new CircularQueue<String>(String.class, capacity);
    }

    public TaskVariable(UUID id, TaskVariable taskVariable, int capacity){
        this(id, taskVariable.getDiagramId(), taskVariable.getKey(), capacity);
        this.queue.add(taskVariable.getValues());
    }

    public UUID getDiagramId() { return this.diagramId; }

    public String getKey() { return this.key; }

    public void add(String value){
        this.queue.add(value);
    }

    public String getLast(){
        return this.queue.getLast();
    }

    public List<String> getValues(){
        return this.queue.getList();
    }

}
