package emr.analytics.service.jobs;

import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.service.SourceBlocks;
import emr.analytics.service.messages.JobRequest;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public abstract class AnalyticsJob implements Serializable {

    protected UUID _id;
    protected JobMode _mode;
    protected String _diagramName;
    protected String _source;
    protected LogLevel _logLevel = LogLevel.Progress;

    public AnalyticsJob(JobRequest request, String template, HashMap<String, Definition> definitions){
        this._id = request.getJobId();
        this._mode = request.getJobMode();
        this._diagramName = request.getDiagram().getName();
        this._source = this.compile(request, template, definitions);
    }

    public String getDiagramName(){
        return _diagramName;
    }

    public String getName(){ return _diagramName + "_" + _mode.toString(); }

    public void setLogLevel(LogLevel level){
        _logLevel = level;
    }

    public LogLevel getLogLevel(){ return _logLevel; }

    public UUID getId(){ return _id; }

    public String getSource(){ return _source; }

    public JobMode getJobMode() { return _mode; }

    protected String compile(JobRequest request, String template, HashMap<String, Definition> definitions){

        String source = "";

        Diagram diagram = request.getDiagram();

        // compile a list of blocks to execute
        SourceBlocks sourceBlocks = new SourceBlocks();

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
                sourceBlocks.add(block, diagram.getLeadingWires(block.getUniqueName()));

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