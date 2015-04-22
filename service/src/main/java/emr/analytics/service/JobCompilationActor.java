package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.service.jobs.PythonJob;
import emr.analytics.service.messages.JobRequest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class JobCompilationActor extends AbstractActor {

    // todo: read path in from configuration
    private String _path = "../algorithms";

    public static Props props(){ return Props.create(JobCompilationActor.class); }

    public JobCompilationActor(){

        receive(ReceiveBuilder.
            match(JobRequest.class, request -> {

                // todo: current this only handles python compilation

                // compile diagram
                Diagram diagram = request.getDiagram();
                String source = this.compile(diagram);

                // create file name
                String fileName = this.writeSourceFile(request.getJobId(), source);

                PythonJob job = new PythonJob(request.getJobId(), diagram.getName(), fileName);

                sender().tell(job, self());
            }).build()
        );
    }

    private String compile(Diagram diagram){

        String source = "";

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
                sourceBlocks.add(block, diagram.getLeadingWires(block.getName()));

                for (Block next : diagram.getNext(block.getName())) {
                    queue.add(next);
                }
            }
        }

        // compile configured blocks
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

    private String writeSourceFile(UUID id, String source){
        String fileName = getFileName(id);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(source);
            out.close();
        }
        catch(IOException ex) {
            System.err.println("IO Exception occurred.");
        }

        return fileName;
    }

    private String getFileName(UUID id){
        return String.format("%s/%s.py", _path, id.toString());
    }
}
