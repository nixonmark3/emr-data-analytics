package emr.analytics.models.state;

import emr.analytics.models.diagram.Diagram;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiagramState {

    private String name = "";
    private int state = 0;
    private UUID jobId = null;
    private List<BlockState> blockStates = new ArrayList<BlockState>();

    private DiagramState() {}

    public DiagramState(String name, UUID jobId) {
        this.name = name;
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public List<BlockState> getBlockStates() {
        return blockStates;
    }

    public void setBlockStates(List<BlockState> blockStates) {
        this.blockStates = blockStates;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public static DiagramState Create(Diagram diagram, UUID jobId) {
        DiagramState diagramState = new DiagramState(diagram.getName(), jobId);

        List<BlockState> blockStateList = new ArrayList<BlockState>();
        diagram.getBlocks().stream().forEach(b -> blockStateList.add(new BlockState(b.getId(), b.getName(), b.getState())));

        diagramState.setBlockStates(blockStateList);

        return diagramState;
    }

    public void setBlockState(UUID blockId, int blockState) {
        this.blockStates.stream()
                .filter(b -> b.getId().equals(blockId))
                .forEach(b -> b.setState(blockState));
    }
}
