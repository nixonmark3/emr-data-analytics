package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import emr.analytics.models.messages.TaskVariable;
import emr.analytics.service.consumers.ConsumerDispatcher;
import emr.analytics.service.kafka.ConsumerTask;

import emr.analytics.service.messages.ConsumerStart;
import emr.analytics.service.messages.ConsumerStop;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Executors;

/**
 * Kafka topic consumer: consumer tasks are created on start of an online task
 * This actor listens to a configured kafka topic and performs the actions specified
 * in the consumer task.
 */
public class KafkaConsumer extends AbstractActor {

    // initialize logger
    private static final Logger logger = Logger.getLogger(KafkaConsumer.class);

    // kafka consumer
    private ConsumerConnector consumer;
    // reference to the client actor
    private ActorRef client;
    // running flag
    private boolean running;
    // configured online topic
    private String topic;
    // dictionary of consumer tasks mapped by diagram id
    private Map<UUID, ConsumerTask> consumerTasks;

    public static Props props() { return Props.create(KafkaConsumer.class); }

    /**
     * Constructor: initialize fields and actor messages
     */
    public KafkaConsumer(){

        // initialize the running flag
        this.running = false;

        // initialize map of consumer tasks
        this.consumerTasks = new HashMap<>();

        Properties properties = TaskProperties.getInstance().getProperties();
        this.topic = properties.getProperty("kafka.online.topic");

        try {
            // instantiate a kafka producer
            this.consumer = Consumer.createJavaConsumerConnector(this.getConfig(properties.getProperty("kafka.consumer")));
        }
        catch(Exception ex){
            logger.error(String.format("Exception occurred while instantiating kafka consumer. Details: %s.", ex.toString()));
        }

        receive(ReceiveBuilder

            /**
             * When a reference to the client actor is received start the streaming source job
             */
            .match(ActorRef.class, actor -> {

                this.client = actor;
                run();
            })

            /**
             * Start a consumer task
             */
            .match(ConsumerStart.class, task -> {

                if (!this.consumerTasks.containsKey(task.getDiagramId())) {

                    ConsumerTask consumerTask = new ConsumerTask(
                            new TaskVariable(task.getId(),
                                    task.getDiagramId(),
                                    "ONLINE",
                                    40),
                            task.getConsumers());

                    this.consumerTasks.put(task.getDiagramId(), consumerTask);
                }
            })

            /**
             * Stop a consumer task
             */
            .match(ConsumerStop.class, job -> {

                if (this.consumerTasks.containsKey(job.getDiagramId()))
                    this.consumerTasks.remove(job.getDiagramId());
            })

            /**
             * set the stop flag and send a poison pill
             */
            .match(String.class, s -> s.equals("stop"), s -> {

                // set running flag to false and close the consumer
                this.running = false;
                this.consumer.shutdown();
                // kill this actor
                self().tell(PoisonPill.getInstance(), self());
            })

            .build()
        );
    }

    /**
     * Asynchronously consumer the configured kafka topic
     */
    private void run() {

        this.running = true;

        // Define single thread for topic
        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        topicMap.put(this.topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreamsMap = this.consumer.createMessageStreams(topicMap);

        List<KafkaStream<byte[], byte[]>> streamList = consumerStreamsMap.get(this.topic);

        Executors.newSingleThreadExecutor().submit(() -> {

            while (running) {

                try {

                    for (final KafkaStream<byte[], byte[]> stream : streamList) {

                        ConsumerIterator<byte[], byte[]> iterator = stream.iterator();

                        while (iterator.hasNext()) {

                            MessageAndMetadata<byte[], byte[]> iteration = iterator.next();

                            UUID diagramId = UUID.fromString(new String(iteration.key()));
                            String value = new String(iteration.message());

                            if (this.client != null && this.consumerTasks.containsKey(diagramId)) {

                                ConsumerTask consumerTask = this.consumerTasks.get(diagramId);
                                TaskVariable variable = consumerTask.getTaskVariable();
                                variable.add(value);

                                this.client.tell(variable, self());

                                Executors.newSingleThreadExecutor().submit(() -> ConsumerDispatcher.send(value, consumerTask.getConsumers()));
                            }
                        }
                    }
                }
                catch (Exception ex) {

                    logger.error(String.format("Exception occurred while writing to Kafka. Details: %s.", ex.toString()));
                }
            }
        });
    }

    private ConsumerConfig getConfig(String path) {

        Properties props = new Properties();
        props.put("zookeeper.connect", path);
        props.put("group.id", "service");
        props.put("zookeeper.session.timeout.ms", "500");
        props.put("zookeeper.sync.time.ms", "250");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }
}
