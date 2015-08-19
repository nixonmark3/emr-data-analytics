package emr.analytics.service.consumers;

public abstract class DataConsumer {

    public static void send(String consumerType, ConsumerData consumerData) {

        DataConsumer dataConsumer = null;

        switch(consumerType) {

            case "OPC":
                dataConsumer = new OpcDataConsumer();
                break;

            case "PI":
                dataConsumer = new PiDataConsumer();
                break;

            case "Simulated":
                dataConsumer = new SimulatedDataConsumer();
                break;

            default:
                throw new ConsumerException(String.format("The specified consumer type, %s, is not supported.", consumerType));
        }

        if (dataConsumer != null) {

            dataConsumer.send(consumerData);
        }
    }

    protected abstract void send(ConsumerData consumerData);
}
