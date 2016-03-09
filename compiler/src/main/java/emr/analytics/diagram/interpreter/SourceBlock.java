package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.WireSummary;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class SourceBlock {

    protected TargetEnvironments targetEnvironment;
    protected Block block;
    protected List<WireSummary> wires;

    public SourceBlock(TargetEnvironments targetEnvironment, Block block, List<WireSummary> wires){
        this.targetEnvironment = targetEnvironment;
        this.block = block;
        this.wires = wires;
    }

    public String getCode() { return this.getCode(null); }

    public abstract String getCode(Set<UUID> compiled);

    public abstract Package[] getPackages();
}
