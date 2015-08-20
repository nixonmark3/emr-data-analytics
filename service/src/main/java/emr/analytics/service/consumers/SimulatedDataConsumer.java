package emr.analytics.service.consumers;

import emr.analytics.service.consumers.serializers.Consumer;
import java.time.LocalDateTime;

public class SimulatedDataConsumer extends DataConsumer {

    protected void send(String value, Consumer consumer) {

        System.out.printf("Time: %s, ip: %s, Tag: %s, Value: %s.\n",
                LocalDateTime.now().toString(),
                consumer.getIp(),
                consumer.getTag(),
                value);
    }
}
