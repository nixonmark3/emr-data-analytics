package emr.analytics.service.sources.serializers;

import java.io.Serializable;
import java.util.List;

public class PiResponse implements Serializable {

    private String tag;
    private List<List<Double>> data;

    private PiResponse() {}

    public String getTag() {

        return tag;
    }

    public void setTag(String tag) {

        this.tag = tag;
    }

    public List<List<Double>> getData() {

        return data;
    }

    public void setData(List<List<Double>> data) {

        this.data = data;
    }
}
