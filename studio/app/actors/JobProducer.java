
package actors;

import java.io.*;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import emr.analytics.models.diagram.*;

public class JobProducer {

    private ZContext _context;
    private Socket _socket;
    private String _path = "tcp://127.0.0.1:1237";

    public JobProducer(){
        // establish zmq context
        _context = new ZContext();
        // create a subscription socket
        _socket = _context.createSocket(ZMQ.PUB);

        // connect and subscribe
        _socket.bind(_path);
    }

    public void send(Object payload){

        byte[] bytes = null;
        try {
            bytes = serialize(payload);
        }
        catch(IOException ex){
            System.err.println(String.format("IOException occurred: %s", ex.toString()));
            return;
        }

        _socket.send(bytes, 0);
    }

    public static byte[] serialize(Object obj) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(stream);
        output.writeObject(obj);
        return stream.toByteArray();
    }
}
