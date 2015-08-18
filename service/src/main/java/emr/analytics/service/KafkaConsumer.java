package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaConsumer extends AbstractActor {

    // initialize logger
    private static final Logger logger = Logger.getLogger(KafkaConsumer.class);

    private ConsumerConnector consumer;
    private final String topic = "ONLINE";
    private boolean running;

    public static Props props() { return Props.create(KafkaConsumer.class); }

    public KafkaConsumer(){

        // initialize the running flag
        this.running = false;

        // load kafka properties
        Properties properties = JobServiceHelper.loadProperties("kafka");

        try {
            // instantiate a kafka producer
            this.consumer = Consumer.createJavaConsumerConnector(this.getConfig());
        }
        catch(Exception ex){
            logger.error(String.format("Exception occurred while instantiating kafka consumer. Details: %s.", ex.toString()));
        }

        receive(ReceiveBuilder

            /**
             * start the streaming source job
             */
            .match(String.class, s -> s.equals("start"), s -> {
                run();
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
     * Asynchronously produce kafka records
     */
    private void run(){
        this.running = true;

        // Define single thread for topic
        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        topicMap.put(this.topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreamsMap = this.consumer.createMessageStreams(topicMap);

        List<KafkaStream<byte[], byte[]>> streamList = consumerStreamsMap.get(this.topic);

        // spawn a thread to consume records
        new Thread(() -> {

            while(running){

                try{
                    for (final KafkaStream<byte[], byte[]> stream : streamList) {
                        ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
                        while (iterator.hasNext()){
                            MessageAndMetadata<byte[], byte[]> iteration = iterator.next();
                            System.out.printf("Key: %s, Value: %s.\n",
                                    new String(iteration.key()),
                                    new String(iteration.message()));
                        }
                    }
                }
                catch(Exception ex){
                    logger.error(String.format("Exception occurred while writing to Kafka. Details: %s.", ex.toString()));
                }
            }
        }).start();
    }

    private ConsumerConfig getConfig(){
        Properties props = new Properties();
        props.put("zookeeper.connect", "localhost:2181");
        props.put("group.id", "service");
        props.put("zookeeper.session.timeout.ms", "500");
        props.put("zookeeper.sync.time.ms", "250");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }
}
