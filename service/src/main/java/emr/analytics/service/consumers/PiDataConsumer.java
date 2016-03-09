package emr.analytics.service.consumers;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.time.Instant;

import emr.analytics.models.messages.Consumer;

public class PiDataConsumer extends DataConsumer {

    protected void send(String value, Consumer consumer) {

        try {

            String uri = String.format("http://%s:8003/writevalues", consumer.getPath());

            String payload = String.format("{'items': [{'tag': '%s', 'ts': %s, 'value': '%s', 'type': 'F'}]}",
                    consumer.getKey(),
                    Instant.now().toEpochMilli(),
                    value);

            HttpPost post = new HttpPost(uri);
            post.setEntity(new StringEntity(payload));

            CloseableHttpClient client = HttpClientBuilder.create().build();
            client.execute(post);
            client.close();
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }

    }
}
