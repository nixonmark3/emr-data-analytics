package emr.analytics.diagram.interpreter;

public class CompiledDiagram {

    private String source;
    private String metaData;

    public CompiledDiagram(String source){
        this(source, null);
    }

    public CompiledDiagram(String source, String metaData){
        this.source = source;

        if (metaData != null)
            this.metaData = metaData;
        else
            this.metaData = "";
    }

    public String getSource(){ return this.source; }

    public String getMetaData() { return this.metaData; }
}
