package emr.analytics.service.sources;

import emr.analytics.models.messages.StreamingSourceRequest;

public class SourceFactory {

    public static StreamingSource get(StreamingSourceRequest request) throws SourceException {

        StreamingSource source;
        switch(request.getStreamingSource().getPollingSourceType()){

            case OPC:
                source = new OpcPollingSource(request);
                break;

            case PI:
                source = new PiPollingSource(request);
                break;

            case Simulated:
                source = new SimulatedSource(request);
                break;

            default:
                throw new SourceException(String.format("The specified streaming source type, %s, is not supported.",
                        request.getStreamingSource().getPollingSourceType().toString()));
        }

        return source;
    }
}
