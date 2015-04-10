package actors;

import akka.actor.*;

import java.util.UUID;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The actor we use when a client opens a web socket
 */
public class ClientActor extends UntypedActor
{
    private final UUID _clientId;
    private final ActorRef _out;

    public static Props props(ActorRef out)
    {
        return Props.create(ClientActor.class, out, UUID.randomUUID());
    }

    public ClientActor(ActorRef out, UUID clientId)
    {
        this._out = out;
        this._clientId = clientId;

        ObjectNode clientRegistrationMsg = Json.newObject();
        clientRegistrationMsg.put("messageType", "ClientRegistration");
        clientRegistrationMsg.put("id", this._clientId.toString());
        this._out.tell(clientRegistrationMsg, self());
    }

    @Override
    public void preStart() {
        System.out.println("Pre-Starting Client Actor");
        ClientActorManager.getInstance().addClientActorPath(this._clientId, this._out.path());
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("Pre-Stopping Client Actor");
        ClientActorManager.getInstance().removeClientActorPath(this._clientId);
    }

    public void onReceive(Object message) throws Exception
    {
    }
}
