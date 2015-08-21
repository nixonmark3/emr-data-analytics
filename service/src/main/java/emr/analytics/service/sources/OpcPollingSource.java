package emr.analytics.service.sources;

import emr.analytics.models.messages.StreamingSourceRequest;

import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

public class OpcPollingSource extends DataSource implements StreamingSource {

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

            Optional<JSONObject> jsonResponse = getDataFromWebService(uri);

            if (jsonResponse.isPresent()) {

                JSONObject json = jsonResponse.get();

                json.keys().forEachRemaining(key -> {

                    JSONObject keyValue = (JSONObject) json.get(key);
                    values.add(key, Double.parseDouble((String) keyValue.get("Val")));
                });
            }
        }
        else {

            // todo need proper error handling mechanism
            System.out.println("OPC data stream source was not configured correctly.");
        }

        return values;
    }


}
