
package actors;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

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

    public void send(String message){
        _socket.send(message.getBytes(), 0);
    }
}
