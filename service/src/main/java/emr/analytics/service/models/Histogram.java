package emr.analytics.service.models;

public class Histogram {

    private String name;
    private int[] data;
    private double[] edges;

    public String getName(){ return name; }
    public int[] getData() { return data; }
    public double[] getEdges() { return edges; }

    private Histogram() {}
}
