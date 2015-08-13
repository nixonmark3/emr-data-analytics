package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StreamingInfos extends BaseMessage implements Serializable {

    private List<StreamingInfo> items;

    public StreamingInfos(){
        super("streaming-infos");

        this.items = new ArrayList<>();
    }

    public List<StreamingInfo> getItems() {
        return this.items;
    }

    public void add(StreamingInfo item){
        items.add(item);
    }
}
