package emr.analytics.models.messages;

import java.io.Serializable;

public class StreamingTerminationRequest extends InputMessage implements Serializable {

    private final String topic;

    public StreamingTerminationRequest(String topic) {
        super("streaming-termination-request");

        this.topic = topic;
    }

    public String getTopic() { return this.topic; }
}