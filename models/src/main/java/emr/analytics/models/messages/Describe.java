package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Describe implements Serializable {

    private List<DescribeFeature> features;

    public Describe(){
        this.features = new ArrayList<DescribeFeature>();
    }

    public void add(DescribeFeature feature){
        features.add(feature);
    }

    public List<DescribeFeature> getFeatures() { return this.features; }

    public static class DescribeFeature implements Serializable {

        private String name;
        private String type;
        private String sourceName;
        private int count;
        private double min;
        private double max;
        private double avg;
        private double stdev;
        private int[] histogram;
        private double[] edges;

        public DescribeFeature(String name, String type, String sourceName){
            this.name = name;
            this.type = type;
            this.sourceName = sourceName;
        }

        public String getName() { return this.name; }
        public String getType() { return this.type; }
        public String getSourceName() { return this.sourceName; }
        public int getCount() { return this.count; }
        public void setCount(int value) { this.count = value; }
        public double getMin() { return this.min; }
        public void setMin(double value) { this.min = value; }
        public double getMax() { return this.max; }
        public void setMax(double value) { this.max = value; }
        public double getAvg() { return this.avg; }
        public void setAvg(double value) { this.avg = value; }
        public double getStdev() { return this.stdev; }
        public void setStdev(double value) { this.stdev = value; }
        public int[] getHistogram() { return this.histogram; }
        public double[] getEdges() { return this.edges; }
        public void setHist(int[] hist, double[] edges) { this.histogram = hist; this.edges = edges; }

        private DescribeFeature() {}
    }
}
