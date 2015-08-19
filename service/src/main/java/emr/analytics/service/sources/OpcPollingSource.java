package emr.analytics.service.sources;

import emr.analytics.models.messages.StreamingSourceRequest;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;

public class OpcPollingSource implements StreamingSource {

    private String url;
    private List<String> keys;

    public OpcPollingSource(StreamingSourceRequest request){
        this.url = request.getStreamingSource().getUrl();
        this.keys = request.getStreamingSource().getKeys();
    }

    public SourceValues<Double> read(){

        SourceValues<Double> values = new SourceValues<Double>();

        if (keys.size() == 1) {

            String uri = String.format("http://%s:8000/jsondata/%s", url, keys.get(0));

            try {

                HttpGet get = new HttpGet(uri);

                CloseableHttpClient client = HttpClientBuilder.create().build();
                CloseableHttpResponse response = client.execute(get);
                HttpEntity httpEntity = response.getEntity();
                Optional<JSONObject> jsonResponse = convertInputStreamToJson(httpEntity.getContent());
                response.close();
                client.close();

                if (jsonResponse.isPresent()) {

                    JSONObject json = jsonResponse.get();

                    json.keys().forEachRemaining(key -> {

                        JSONObject keyValue = (JSONObject) json.get(key);
                        values.add(key, Double.parseDouble((String) keyValue.get("Val")));
                    });
                }
            }
            catch (Exception exception) {

                exception.printStackTrace();
            }
        }
        else {

            // todo need proper error handling mechanism
            System.out.println("OPC data stream source was not configured correctly.");
        }

        return values;
    }

    public Optional<JSONObject> convertInputStreamToJson(InputStream stream) {

        JSONObject json = null;

        try {

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputString;
            while ((inputString = streamReader.readLine()) != null) {

                responseStrBuilder.append(inputString);
            }

            json = new JSONObject(responseStrBuilder.toString());
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }

        return Optional.of(json);
    }
}
