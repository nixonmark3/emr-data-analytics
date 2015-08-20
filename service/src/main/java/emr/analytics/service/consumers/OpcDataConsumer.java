package emr.analytics.service.consumers;

import emr.analytics.service.consumers.serializers.Consumer;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class OpcDataConsumer extends DataConsumer {

    protected void send(String value, Consumer consumer) {

//        try {
//
//            String uri = "http://192.168.17.129:8000/updatedata/InferredCalc1";
//
//            String tag = "PICK_INFER_MEAS/INFER1.CV";
//            String val = consumerData.value.toString();
//
//            HttpPost post = new HttpPost(uri);
//            post.setEntity(new StringEntity(String.format("%s,%s", tag, val)));
//
//            CloseableHttpClient client = HttpClientBuilder.create().build();
//            client.execute(post);
//            client.close();
//        }
//        catch (Exception exception) {
//
//            System.out.println(exception.getStackTrace().toString());
//        }
    }
}
