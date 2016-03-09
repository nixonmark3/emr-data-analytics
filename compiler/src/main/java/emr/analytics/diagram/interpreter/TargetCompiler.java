package emr.analytics.diagram.interpreter;

import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.Wire;
import emr.analytics.models.diagram.WireSummary;

import java.util.*;

public abstract class TargetCompiler {

    protected abstract SourceBlock getSourceBlock(Block block, List<WireSummary> wires);

    public SourceBlock getSourceBlock(Block block){ return this.getSourceBlock(block, null); }

    protected List<SourceBlock> getSourceBlocks(Diagram diagram){

        // build an ordered list of blocks to compile
        List<SourceBlock> sourceBlocks = new ArrayList<>();
        // maintain a set of blocks that have been visited
        Set<UUID> visited = new HashSet<UUID>();

        // Initialize queue of blocks to compile
        Queue<Block> queue = new LinkedList<Block>();
        for (Block block : diagram.getRoot()) {
            visited.add(block.getId());
            queue.add(block);
        }

        // Capture all configured blocks in order
        while (!queue.isEmpty()) {
            Block block = queue.remove();

            // confirm block has been configured
            if (block.getConfigured() || !block.getConfigured()) {

                // add source block to list
                SourceBlock sourceBlock = getSourceBlock(block, diagram.getInputWires(block.getId()));
                if (sourceBlock != null)
                    sourceBlocks.add(sourceBlock);

                // follow the diagram to the next set of blocks
                for (Block next : diagram.getNext(block.getId())) {

                    if (!visited.contains(next.getId())) {

                        // confirm all leading blocks have been previously queued
                        boolean ready = true;
                        List<WireSummary> wires = diagram.getInputWires(next.getId());
                        for(Wire wire : wires){
                            if (!visited.contains(wire.getFrom_node()))
                                ready = false;
                        }

                        if (ready) {
                            queue.add(next);
                            visited.add(next.getId());
                        }
                    }
                }
            }
        }

        return sourceBlocks;
    }

    public abstract String compile(Diagram diagram);
}
