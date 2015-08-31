package emr.analytics.service.consumers;

import emr.analytics.service.consumers.serializers.Consumer;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileDataConsumer extends DataConsumer {

    protected void send(String value, Consumer consumer) {

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(consumer.getIp(), true))){
            writer.write(String.format("%s,%s\n", consumer.getTag(), value));
        }
        catch(Exception ex){
            throw new ConsumerException(ex);
        }
    }
}

