package emr.analytics.models.messages;

import java.io.Serializable;

public class StreamingSourceKillRequest extends BaseMessage implements Serializable {

    private final String topic;

    public StreamingSourceKillRequest(String topic) {
        super("StreamingSourceKillRequest");

        this.topic = topic;
    }

    public String getTopic() { return this.topic; }
}