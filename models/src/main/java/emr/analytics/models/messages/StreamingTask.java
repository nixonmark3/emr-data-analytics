package emr.analytics.models.messages;

import emr.analytics.models.sources.PollingSource;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class StreamingTask extends OutputMessage implements Serializable {

    private String topic;
    private PollingSource.PollingSourceType pollingSourceType;
    private int frequency;
    private Date started = null;

    public StreamingTask(UUID id, String topic, PollingSource.PollingSourceType pollingSourceType, int frequency){
        super(id, null, "streaming-info");

        this.topic = topic;
        this.pollingSourceType = pollingSourceType;
        this.frequency = frequency;
        this.started = new Date();
    }

    public StreamingTask(StreamingTask streamingTask){
        this(streamingTask.getId(), streamingTask.getTopic(), streamingTask.getPollingSourceType(), streamingTask.getFrequency());

        this.started = streamingTask.getStarted();
    }

    public String getTopic(){ return this.topic; }

    public PollingSource.PollingSourceType getPollingSourceType(){ return this.pollingSourceType; }

    public int getFrequency(){ return this.frequency; }

    public Date getStarted(){ return this.started; }

}
