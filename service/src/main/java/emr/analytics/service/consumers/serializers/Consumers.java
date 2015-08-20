package emr.analytics.service.consumers.serializers;

import java.io.Serializable;
import java.util.List;

public class Consumers implements Serializable {

    private List<Consumer> consumers;

    private Consumers() {}

    public List<Consumer> getConsumers() {

        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {

        this.consumers = consumers;
    }
}
