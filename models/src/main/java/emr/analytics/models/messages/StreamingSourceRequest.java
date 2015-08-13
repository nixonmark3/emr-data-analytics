package emr.analytics.models.messages;

import emr.analytics.models.sources.PollingSource;

import java.io.Serializable;

public class StreamingSourceRequest extends BaseMessage implements Serializable {

    private String topic;
    private PollingSource streamingSource;

    public StreamingSourceRequest(String topic, PollingSource streamingSource){
        this();

        this.topic = topic;
        this.streamingSource = streamingSource;
    }

    public String getTopic(){ return this.topic; }

    public PollingSource getStreamingSource(){ return this.streamingSource; }

    private StreamingSourceRequest(){ super("StreamingSourceRequest"); }
}
