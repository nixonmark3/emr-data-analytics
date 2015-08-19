package emr.analytics.service.consumers;

public class SimulatedDataConsumer extends DataConsumer {

    protected void send(ConsumerData consumerData) {

        System.out.printf("Consumer: %s, Value: %s.\n",
                consumerData.ip.toString(),
                consumerData.value.toString());
    }
}
