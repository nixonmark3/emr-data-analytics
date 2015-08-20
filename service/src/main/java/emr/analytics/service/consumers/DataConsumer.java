package emr.analytics.service.consumers;

import emr.analytics.service.consumers.serializers.Consumer;

public abstract class DataConsumer {

    protected abstract void send(String value, Consumer consumer);
}
