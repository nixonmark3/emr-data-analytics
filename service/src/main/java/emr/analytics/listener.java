package emr.analytics;

import java.io.*;
import java.util.*;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import emr.analytics.models.diagram.*;

import javax.xml.transform.Source;

public class listener implements Runnable {
    private ZContext _context;
    private Socket _socket;
    private String _path = "tcp://127.0.0.1:1237";

    public listener(){
        // establish zmq context
        _context = new ZContext();
        // create a subscription socket
        _socket = _context.createSocket(ZMQ.SUB);

        // connect and subscribe
        _socket.connect(_path);
        _socket.subscribe("".getBytes());
    }

    public void run(){

        while (!Thread.currentThread().isInterrupted()) {

            Diagram diagram;
            try {
                diagram = (Diagram) deserialize(_socket.recv(0));
            }
            catch(ClassNotFoundException ex){
                // todo: handle exception
                System.err.println(String.format("Class Not Found Exception occurred: %s.", ex.toString()));

                return;
            }
            catch(IOException ex){
                // todo: handle exception
                System.err.println(String.format("IO Exception occurred: %s.", ex.toString()));

                return;
            }

            System.out.println(String.format("Evaluation Request Received for Diagram: %s.",
                    diagram.getName()));

            // todo: refactor evaluate on non-blocking thread
            evaluate(diagram);
        }

        _socket.close();
        _context.destroy();
    }

    public void evaluate(Diagram diagram){
        // compile a list of blocks to execute
        SourceBlocks sourceBlocks = new SourceBlocks(diagram);

        // Initialize queue of blocks to compile
        Queue<Block> queue = new LinkedList<Block>();
        for (Block block : diagram.getRoot())
            queue.add(block);

        // Capture all configured blocks in order
        while (!queue.isEmpty()){

            Block block = queue.remove();

            // Capture configured blocks and queue descending blocks
            if (block.isConfigured()){

                sourceBlocks.add(block, diagram.getLeadingWires(block.getName()));

                for (Block next : diagram.getNext(block.getName()))
                    queue.add(next);
            }
        }

        // compile and execute configured blocks
        if (!sourceBlocks.isEmpty()) {

            String source = "";
            try {
                source = sourceBlocks.compile("python_driver.mustache");
            } catch (IOException ex) {
                System.err.println(String.format("IOException: %s.", ex.toString()));
            }

            // todo: temporarily print generated python code
            System.out.println(source);

            PythonTask task = new PythonTask(source);
            task.execute();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {

        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        ObjectInputStream input = new ObjectInputStream(stream);
        return input.readObject();
    }

}
