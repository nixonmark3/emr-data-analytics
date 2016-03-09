package emr.analytics.service.consumers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;

import emr.analytics.models.messages.Consumer;

public class LogConsumer extends DataConsumer {

    protected void send(String value, Consumer consumer) {

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(consumer.getPath(), true))){
            writer.write(String.format("%s: %s,%s\n", LocalDateTime.now().toString(), consumer.getKey(), value));
        }
        catch(Exception ex){
            throw new ConsumerException(ex);
        }
    }
}

