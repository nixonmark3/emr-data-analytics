package models;

import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;

import java.util.UUID;

public class LoadRequest {

    private UUID diagramId;
    private UUID sessionId;
    private String diagramName;
    private TargetEnvironments targetEnvironment;
    private Block block;

    public UUID getDiagramId() { return this.diagramId; }

    public UUID getSessionId() { return this.sessionId; }

    public String getDiagramName() { return this.diagramName; }

    public TargetEnvironments getTargetEnvironment() { return this.targetEnvironment; }

    public Block getBlock() { return this.block; }

    public LoadRequest(){}
}
