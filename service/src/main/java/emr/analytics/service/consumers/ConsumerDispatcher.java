package emr.analytics.service.consumers;

import emr.analytics.models.messages.Consumers;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerDispatcher {

    public static void send(String value, Consumers consumers) {

        // verify that consumers exist
        if (consumers == null)
            return;

        int threadCount = 10;

        try {

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

            consumers.getConsumers().stream().forEach(consumer -> {

                executorService.submit(() -> {

                    Optional<DataConsumer> dataConsumer = ConsumerFactory.get(consumer.getConsumerType());

                    if (dataConsumer.isPresent()) {

                        dataConsumer.get().send(value, consumer);
                    }
                });
            });

            executorService.shutdown();
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }
    }
}
