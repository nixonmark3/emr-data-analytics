package actors;

import akka.actor.*;

import java.util.UUID;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The actor we use when a client opens a web socket
 */
public class ClientActor extends UntypedActor {

    public static Props props(ActorRef out) {
        return Props.create(ClientActor.class, out, UUID.randomUUID().toString());
    }

    public ClientActor(ActorRef out, String clientId) {
        this.out = out;
        this.clientId = clientId;

        // Send client id back to the client
        ObjectNode id = Json.newObject();
        id.put("id", this.clientId);
        this.out.tell(id, self());
    }

    @Override
    public void preStart() {
        ClientActorManager.getInstance().addClientActorPath(this.clientId, this.out.path());
    }

    @Override
    public void postStop() throws Exception {
        ClientActorManager.getInstance().removeClientActorPath(this.clientId);
    }

    public void onReceive(Object message) throws Exception {
        // todo handle messages that this actor receives

        // todo can we use akka actors to make this asynchronous?
    }

    private final String clientId;
    private final ActorRef out;
}
