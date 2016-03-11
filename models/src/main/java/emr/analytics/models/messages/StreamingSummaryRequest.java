package emr.analytics.models.messages;

import java.io.Serializable;

public class StreamingSummaryRequest extends InputMessage implements Serializable {

    public StreamingSummaryRequest(){
        super(null, "streaming-summary-request");
    }
}