package emr.analytics.diagram.interpreter;

public class DataSource {
    private DataSourceTypes dataSourceType;
    private String name;
    private int progress;
    private String contentType;
    private String path;

    public DataSource(){}

    public DataSourceTypes getDataSourceType(){ return this.dataSourceType; }
    public String getName(){ return this.name; }
    public int getProgress(){ return this.progress; }
    public String getContentType() { return this.contentType; }
    public String getPath(){ return this.path; }
}
