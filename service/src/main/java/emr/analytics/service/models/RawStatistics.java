package emr.analytics.service.models;

import java.util.List;

public class RawStatistics<T> {

    private List<String> columns;
    private List<String> index;
    private List<List<T>> data;

    public List<String> getColumns(){ return this.columns; }
    public List<String> getIndex(){ return this.index; }
    public List<List<T>> getData() { return this.data; }

    public RawStatistics(){}
}
