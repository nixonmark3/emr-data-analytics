package emr.analytics.models.messages;

import emr.analytics.models.sources.PollingSource;

import java.io.Serializable;

public class StreamingRequest extends InputMessage implements Serializable {

    private String topic;
    private PollingSource streamingSource;

    public StreamingRequest(String topic, PollingSource streamingSource){
        this();

        this.topic = topic;
        this.streamingSource = streamingSource;
    }

    public String getTopic(){ return this.topic; }

    public PollingSource getStreamingSource(){ return this.streamingSource; }

    private StreamingRequest(){ super("streaming-request"); }
}
