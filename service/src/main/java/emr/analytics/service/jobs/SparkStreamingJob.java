package emr.analytics.service.jobs;

import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.service.StreamingSourceBlocks;
import emr.analytics.service.messages.JobRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SparkStreamingJob extends AnalyticsJob {

    public SparkStreamingJob(JobRequest request, HashMap<String, Definition> definitions){
        super(request, "sparkstreaming_driver.mustache", definitions);
    }

    @Override
    protected String compile(JobRequest request, String template, HashMap<String, Definition> definitions){

        String source = "";

        Diagram diagram = request.getDiagram();

        // compile a list of blocks to execute
        StreamingSourceBlocks sourceBlocks = new StreamingSourceBlocks(request.getModels());

        // Initialize queue of blocks to compile
        Queue<Block> queue = new LinkedList<Block>();
        for (Block block : diagram.getRoot()) {
            queue.add(block);
        }

        // Capture all configured blocks in order
        while (!queue.isEmpty()) {
            Block block = queue.remove();

            // Capture configured blocks and queue descending blocks
            if (block.isConfigured()) {
                Definition definition = definitions.get(block.getDefinition());

                sourceBlocks.add(definition, block, diagram.getLeadingWires(block.getUniqueName()));

                for (Block next : diagram.getNext(block.getUniqueName())) {
                    queue.add(next);
                }
            }
        }

        // compile configured blocks
        if (!sourceBlocks.isEmpty()) {
            try {
                source = sourceBlocks.compile(template);
            }
            catch (IOException ex) {
                System.err.println(String.format("IOException: %s.", ex.toString()));
            }

            // todo: temporarily print generated code
            System.out.println(source);
        }

        return source;
    }
}
