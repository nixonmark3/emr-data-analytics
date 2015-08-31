package emr.analytics.service.consumers.serializers;

import java.io.Serializable;

public class Consumer implements Serializable {

    private String ip;
    private String tag;
    private ConsumerType consumerType;

    private Consumer() {}

    public String getIp() {

        return ip;
    }

    public void setIp(String ip) {

        this.ip = ip;
    }

    public String getTag() {

        return tag;
    }

    public void setTag(String tag) {

        this.tag = tag;
    }

    public ConsumerType getConsumerType() {

        return consumerType;
    }

    public void setConsumerType(ConsumerType consumerType) {

        this.consumerType = consumerType;
    }

    public enum ConsumerType {
        File,
        Simulated,
        OPC,
        PI
    }
}
