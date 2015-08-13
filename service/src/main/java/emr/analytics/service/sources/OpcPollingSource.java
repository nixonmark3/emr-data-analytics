package emr.analytics.service.sources;

import emr.analytics.models.messages.StreamingSourceRequest;

import java.util.ArrayList;
import java.util.List;

public class OpcPollingSource implements StreamingSource {

    private String url;
    private List<String> keys;

    public OpcPollingSource(StreamingSourceRequest request){
        this.url = request.getStreamingSource().getUrl();
        this.keys = request.getStreamingSource().getKeys();
    }

    public SourceValues<Double> read(){

        // todo: read from opc source

        SourceValues<Double> values = new SourceValues<Double>();
        return values;
    }
}
