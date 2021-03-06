package emr.analytics.service.sources;

import emr.analytics.models.messages.StreamingSourceRequest;

import java.util.List;
import java.util.stream.Collectors;

public class SimulatedSource implements StreamingSource {

    private List<String> keys;

    public SimulatedSource(StreamingSourceRequest request){
        keys = request.getStreamingSource().getKeys();
    }

    public SourceValues<Double> read(){
        return new SourceValues<Double>(keys.stream()
                .map(x -> new SourceValue<Double>(x, Math.random()))
                .collect(Collectors.toList()));
    }
}
