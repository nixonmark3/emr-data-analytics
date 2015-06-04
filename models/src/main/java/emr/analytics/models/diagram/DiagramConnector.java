package emr.analytics.models.diagram;

import java.util.UUID;

public class DiagramConnector {

    private UUID blockId;
    private String type;
    private String name;

    private DiagramConnector(){}

    public DiagramConnector(UUID blockId, String name, String type){
        this.blockId = blockId;
        this.type = type;
        this.name = name;
    }

    public UUID getBlockId(){ return this.blockId; }

    public String getType(){ return this.type; }

    public String getName(){ return this.name; }
}
