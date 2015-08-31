package emr.analytics.models.sources;

import java.io.Serializable;
import java.util.List;

public class PollingSource implements Serializable {

    private PollingSourceType pollingSourceType;
    private List<String> keys;
    private int frequency;
    private String url;

    public PollingSource(PollingSourceType pollingSourceType, String url, int frequency, List<String> keys){
        this.pollingSourceType = pollingSourceType;
        this.frequency = frequency;
        this.url = url;
        this.keys = keys;
    }

    public enum PollingSourceType { File, OPC, PI, Simulated }

    public PollingSourceType getPollingSourceType(){ return this.pollingSourceType; }

    public List<String> getKeys() { return this.keys; }

    public int getFrequency() { return this.frequency; }

    public String getUrl() { return this.url; }

    private PollingSource() {}
}
