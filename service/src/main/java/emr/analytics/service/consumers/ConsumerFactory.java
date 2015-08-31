package emr.analytics.service.consumers;

import emr.analytics.service.consumers.serializers.Consumer.ConsumerType;

import java.util.Optional;

public class ConsumerFactory {

    public static Optional<DataConsumer> get(ConsumerType consumerType) throws ConsumerException {

        DataConsumer dataConsumer = null;

        switch(consumerType) {

            case File:
                dataConsumer = new FileDataConsumer();
                break;

            case OPC:
                dataConsumer = new OpcDataConsumer();
                break;

            case PI:
                dataConsumer = new PiDataConsumer();
                break;

            case Simulated:
                dataConsumer = new SimulatedDataConsumer();
                break;

            default:
                throw new ConsumerException(String.format("The specified consumer type, %s, is not supported.", consumerType));
        }

        return Optional.of(dataConsumer);
    }
}
