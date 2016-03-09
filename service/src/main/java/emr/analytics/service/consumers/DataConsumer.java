package emr.analytics.service.consumers;

import emr.analytics.models.messages.Consumer;

public abstract class DataConsumer {

    protected abstract void send(String value, Consumer consumer);
}
