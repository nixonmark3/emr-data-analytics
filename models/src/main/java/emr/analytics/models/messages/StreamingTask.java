package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class StreamingTask extends OutputMessage implements Serializable {

    private String topic;
    private StreamingSourceType sourceType;
    private String path;
    private int frequency;
    private List<String> keys;
    private Date started = null;

    public StreamingTask(UUID id, String topic, StreamingSourceType sourceType, String path, int frequency, List<String> keys, Date started){
        super(id, null, "streaming-task");

        this.topic = topic;
        this.sourceType = sourceType;
        this.path = path;
        this.frequency = frequency;
        this.keys = keys;
        this.started = started;
    }

    public StreamingTask(StreamingRequest request){
        this(request.getId(), request.getTopic(), request.getSourceType(), request.getPath(), request.getFrequency(), request.getKeys(), new Date());
    }

    public StreamingTask(StreamingTask streamingTask){
        this(streamingTask.getId(), streamingTask.getTopic(), streamingTask.getSourceType(), streamingTask.getPath(), streamingTask.getFrequency(), streamingTask.getKeys(), streamingTask.getStarted());
    }

    public String getTopic(){ return this.topic; }

    public StreamingSourceType getSourceType(){ return this.sourceType; }

    public String getPath() { return this.path; }

    public int getFrequency(){ return this.frequency; }

    public List<String> getKeys() { return this.keys; }

    public Date getStarted(){ return this.started; }
}
