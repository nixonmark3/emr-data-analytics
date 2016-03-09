package emr.analytics.service.consumers;

import java.time.LocalDateTime;

import emr.analytics.models.messages.Consumer;

public class OutConsumer extends DataConsumer {

    protected void send(String value, Consumer consumer) {

        System.out.printf("Time: %s, Key: %s, Value: %s.\n",
                LocalDateTime.now().toString(),
                consumer.getKey(),
                value);
    }
}
