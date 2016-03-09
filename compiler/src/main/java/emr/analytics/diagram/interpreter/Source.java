package emr.analytics.diagram.interpreter;

import java.util.ArrayList;
import java.util.List;

public class Source {

    private SourceTypes sourceType;
    private List<DataSource> dataSources = new ArrayList<DataSource>();

    public Source(){}

    public SourceTypes getSourceType(){ return this.sourceType; }
    public List<DataSource> getDataSources() { return this.dataSources; }
}
