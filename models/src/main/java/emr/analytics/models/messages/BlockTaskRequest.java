package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Wire;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockTaskRequest extends TaskRequest implements Serializable {

    private Block block;
    private List<Wire> wires;

    public BlockTaskRequest(UUID sessionId, UUID diagramId, Mode mode, TargetEnvironments targetEnvironment, String diagramName, Block block, List<Wire> wires){
        super(sessionId, diagramId, mode, targetEnvironment, diagramName, "");

        this.block = block;
        this.wires = wires;
    }

    public BlockTaskRequest(UUID sessionId, UUID diagramId, Mode mode, TargetEnvironments targetEnvironment, String diagramName, Block block){
        this(sessionId, diagramId, mode, targetEnvironment, diagramName, block, new ArrayList<Wire>());
    }

    public Block getBlock() { return this.block; }

    public List<Wire> getWires() { return this.wires; }
}
