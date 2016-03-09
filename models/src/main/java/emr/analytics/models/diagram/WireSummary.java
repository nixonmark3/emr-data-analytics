package emr.analytics.models.diagram;

public class WireSummary extends Wire {

    private String fromNodeName;
    private String toNodeName;

    public WireSummary(Wire wire, String from, String to){
        super(wire.getFrom_node(), wire.getFrom_connector(), wire.getFrom_connectorIndex(), wire.getTo_node(), wire.getTo_connector(), wire.getTo_connectorIndex());

        this.fromNodeName = from;
        this.toNodeName = to;
    }

    public String getFromNodeName(){
        return this.fromNodeName;
    }

    public String getToNodeName(){
        return this.toNodeName;
    }
}
