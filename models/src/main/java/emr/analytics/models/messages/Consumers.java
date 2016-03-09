package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.List;

public class Consumers implements Serializable {

    private List<Consumer> consumers;

    private Consumers() {}

    public List<Consumer> getConsumers() { return this.consumers; }
}
