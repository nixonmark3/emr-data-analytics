package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Features implements Serializable {

    List<Feature> features = new ArrayList<>();

    public void add(Feature feature) { features.add(feature); }

    public List<Feature> getFeatures() { return this.features; }
}
