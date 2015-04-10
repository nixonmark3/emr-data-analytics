package emr.analytics;

import java.io.*;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import emr.analytics.models.messages.EvaluationRequest;

public class listener implements Runnable
{
    private ZContext _context;
    private Socket _socket;
    private String _path = "tcp://127.0.0.1:1237";

    public listener()
    {
        // establish zmq context
        _context = new ZContext();
        // create a subscription socket
        _socket = _context.createSocket(ZMQ.SUB);

        // connect and subscribe
        _socket.connect(_path);
        _socket.subscribe("".getBytes());
    }

    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            EvaluationRequest evaluationRequest;

            try
            {
                evaluationRequest = (EvaluationRequest)deserialize(_socket.recv(0));
            }
            catch(ClassNotFoundException ex)
            {
                // todo: handle exception
                System.err.println(String.format("Class Not Found Exception occurred: %s.", ex.toString()));
                return;
            }
            catch(IOException ex)
            {
                // todo: handle exception
                System.err.println(String.format("IO Exception occurred: %s.", ex.toString()));
                return;
            }

            System.out.println(String.format("Evaluation Request Received for Diagram: %s.", evaluationRequest.getDiagram().getName()));

            new Thread(new DiagramEvaluator(evaluationRequest.getJobId(), evaluationRequest.getDiagram())).start();
        }

        _socket.close();

        _context.destroy();
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException
    {
        return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }
}
