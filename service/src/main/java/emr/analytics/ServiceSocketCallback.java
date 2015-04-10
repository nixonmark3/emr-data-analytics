package emr.analytics;

import java.io.IOException;
import java.net.URI;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class ServiceSocketCallback
{
    private Session _userSession = null;

    public ServiceSocketCallback()
    {
        try
        {
            URI endpointURI = new URI("ws://localhost:9000/getServiceSocket");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession)
    {
        System.out.println("opening websocket");
        this._userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason)
    {
        System.out.println("closing websocket");
        this._userSession = null;
    }

    public void sendMessage(Object message)
    {
        this._userSession.getAsyncRemote().sendObject(message);
    }

    public void close()
    {
        try
        {
            if (this._userSession.isOpen())
            {
                this._userSession.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}