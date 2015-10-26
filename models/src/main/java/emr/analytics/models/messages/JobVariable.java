package emr.analytics.models.messages;

import emr.analytics.models.structures.CircularQueue;

import java.io.Serializable;
import java.util.List;

public class JobVariable extends OutputMessage implements Serializable {

    private String key;
    private final int capacity = 20;
    private CircularQueue<String> queue;

    public JobVariable(String key){
        super("job-variable");

        this.key = key;
        this.queue = new CircularQueue<String>(String.class, this.capacity);
    }

    public JobVariable(JobVariable jobVariable){
        this(jobVariable.getKey());
        this.queue.add(jobVariable.getValues());
    }

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
