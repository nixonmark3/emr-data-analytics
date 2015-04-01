package emr.analytics;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

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

            String message = new String(_socket.recv(0));
            System.out.println(String.format("Received: %s.", message));
        }

        _socket.close();
        _context.destroy();
    }
}
