package emr.analytics.models.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Describe implements Serializable {

    private List<DescribeFeature> features;

    public Describe(Collection<DescribeFeature> features){
        this.features = new ArrayList<DescribeFeature>(features);
    }

    public List<DescribeFeature> getFeatures() { return this.features; }

    private Describe(){}

    public static class DescribeFeature implements Serializable {

        private String name;
        private String type;
        private int count;
        private double min;
        private double max;
        private double avg;
        private double stdev;

        public DescribeFeature(String name, String type){
            this.name = name;
            this.type = type;
        }

        public String getName() { return this.name; }
        public String getType() { return this.type; }
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

        private DescribeFeature() {}
    }
}
