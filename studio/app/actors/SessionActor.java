package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import emr.analytics.models.messages.Ping;
import play.libs.Json;
import services.AnalyticsService;

import java.util.UUID;

public class SessionActor extends AbstractActor {

    private final ActorRef out;
    private UUID id;

    public static Props props(ActorRef out, UUID id)
    {
        return Props.create(SessionActor.class, out, id);
    }

    public SessionActor(ActorRef out, UUID id) {
        this.out = out;
        this.id = id;

        receive(ReceiveBuilder

            // ping request from the client
            .match(JsonNode.class, node -> (node.get("type").asText().equals("ping")), node -> {

                AnalyticsService.getInstance().send(new Ping(), self());
            })

            // subscribe to updates for a specific diagram
            .match(JsonNode.class, node -> (node.get("type").asText().equals("subscribe")), node -> {

                UUID diagramId = UUID.fromString(node.get("id").asText());
                SessionManager.getInstance().subscribe(id, diagramId);
            })

            // ping response from the analytics actor
            .match(Ping.class, ping -> {

                out.tell(Json.toJson(ping), self());
            })

            .matchAny(this::unhandled)
            .build()
        );
    }

    @Override
    public void preStart() {

        // add this actor to the session manager
        SessionManager.getInstance().addSession(id, out);
    }

    @Override
    public void postStop() throws Exception {

        // remove this actor from the session manager
        SessionManager.getInstance().removeSession(id);
    }
}
