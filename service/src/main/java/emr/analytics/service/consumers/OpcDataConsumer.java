package emr.analytics.service.consumers;

import emr.analytics.service.consumers.serializers.Consumer;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Arrays;

public class OpcDataConsumer extends DataConsumer {

    protected void send(String value, Consumer consumer) {

        try {

            String[] parts = consumer.getTag().split("/");

            String uri = String.format("http://%s:8000/updatedata/%s", consumer.getIp(), parts[0]);

            String[] tagParts = Arrays.copyOfRange(parts, 1, parts.length);
            String tag = String.join("/", tagParts);

            HttpPost post = new HttpPost(uri);
            post.setEntity(new StringEntity(String.format("%s,%s", tag, value)));

            CloseableHttpClient client = HttpClientBuilder.create().build();
            client.execute(post);
            client.close();
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }
    }
}
