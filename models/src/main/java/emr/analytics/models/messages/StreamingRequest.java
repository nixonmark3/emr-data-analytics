package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.List;

public class StreamingRequest extends InputMessage implements Serializable {

    private String topic;
    private StreamingSourceType sourceType;
    private String path;
    private int frequency;
    private List<String> keys;

    public StreamingRequest(String topic, StreamingSourceType sourceType, String path, int frequency, List<String> keys){
        super(null, "streaming-request");

        this.topic = topic;
        this.sourceType = sourceType;
        this.path = path;
        this.frequency = frequency;
        this.keys = keys;
    }

    public String getTopic(){ return this.topic; }

    public StreamingSourceType getSourceType(){ return this.sourceType; }

    public String getPath() { return this.path; }

    public int getFrequency() { return this.frequency; }

    public List<String> getKeys() { return this.keys; }

    private StreamingRequest(){ super(null, "streaming-request"); }
}