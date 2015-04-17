package emr.analytics;

import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class DiagramEvaluator implements Runnable {
    private Diagram _diagram = null;
    private UUID _jobId = null;

    public DiagramEvaluator(UUID jobId, Diagram diagram) {
        this._jobId = jobId;
        this._diagram = diagram;
    }

    public void run() {
        PythonTask task = new PythonTask(this._jobId, evaluate());
        task.execute();
    }

    private String evaluate() {
        String source = "";

        // compile a list of blocks to execute
        SourceBlocks sourceBlocks = new SourceBlocks();

        // Initialize queue of blocks to compile
        Queue<Block> queue = new LinkedList<Block>();
        for (Block block : this._diagram.getRoot()) {
            queue.add(block);
        }

        // Capture all configured blocks in order
        while (!queue.isEmpty()) {
            Block block = queue.remove();

            // Capture configured blocks and queue descending blocks
            if (block.isConfigured()) {
                sourceBlocks.add(block, this._diagram.getLeadingWires(block.getName()));

                for (Block next : this._diagram.getNext(block.getName())) {
                    queue.add(next);
                }
            }
        }

        // compile and execute configured blocks
        if (!sourceBlocks.isEmpty()) {
            try {
                source = sourceBlocks.compile("python_driver.mustache");
            }
            catch (IOException ex) {
                System.err.println(String.format("IOException: %s.", ex.toString()));
            }

            // todo: temporarily print generated python code
            System.out.println(source);
        }

        return source;
    }
}
