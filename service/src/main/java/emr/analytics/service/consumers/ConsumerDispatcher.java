package emr.analytics.service.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;

import emr.analytics.service.consumers.serializers.Consumers;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerDispatcher {

    private static int nThreads = 10;

    public static void send(String value, String consumersJsonString) {

        try {

            Consumers consumers = new ObjectMapper().readValue(consumersJsonString, Consumers.class);

            ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

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
