package emr.analytics.models.messages;

import java.io.Serializable;

public class Consumer implements Serializable {

    private ConsumerType consumerType;
    private String path;
    private String key;

    private Consumer() {}

    public ConsumerType getConsumerType() { return this.consumerType; }

    public String getPath() { return this.path; }

    public String getKey() { return this.key; }

    public enum ConsumerType {
        DATABASE, LOG, OUT, OPC, PI
    }
}
