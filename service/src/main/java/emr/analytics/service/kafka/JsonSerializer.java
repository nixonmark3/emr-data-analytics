package emr.analytics.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.log4j.Logger;

public class JsonSerializer implements Serializer<Object> {

    private static final Logger logger = Logger.getLogger(JsonSerializer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonSerializer(){}

    @Override
    public void close(){ }

    @Override
    public void configure(java.util.Map<java.lang.String,?> configs, boolean isKey){ }

    @Override
    public byte[] serialize(String topic, Object object) {
        try {
            return objectMapper.writeValueAsString(object).getBytes();
        } catch (JsonProcessingException ex) {
            logger.error(String.format("Json processing failed for object: %s", object.getClass().getName()), ex);
        }
        return "".getBytes();
    }
}
