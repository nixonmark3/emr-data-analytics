package actors;

import akka.actor.*;

import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.DiagramStates;
import controllers.Diagrams;

import emr.analytics.models.messages.EvaluationStatus;
import emr.analytics.models.messages.OnlineNotification;
import emr.analytics.models.state.DiagramState;

import java.util.UUID;

public class ServiceActor extends AbstractActor
{
    private final ActorRef out;

    public static Props props(ActorRef out)
    {
        return Props.create(ServiceActor.class, out);
    }

    public ServiceActor(ActorRef out) {

        this.out = out;

        receive(ReceiveBuilder
            .match(JsonNode.class, j -> j.has("blockStatusList"), j -> {

                EvaluationStatus evaluationStatus = new ObjectMapper().treeToValue((JsonNode) j, EvaluationStatus.class);
                System.out.println(evaluationStatus.getJobId() + " - " + evaluationStatus.getState());

                DiagramState diagramState = DiagramStates.getDiagramState(evaluationStatus.getJobId());

                if (diagramState != null) {
                    diagramState.setState(evaluationStatus.getState());

                    if (evaluationStatus.getState() == 0) {
                        evaluationStatus.getBlockStatusList().stream()
                                .forEach(b -> diagramState.setBlockState(b.getId(), b.getState()));
                    }

                    DiagramStates.saveDiagramState(diagramState);

                    UUID clientId = Diagrams.getClientIdForJob(diagramState.getJobId());

                    ClientActorManager
                            .getInstance()
                            .getClientActor(clientId)
                            .tell(new ObjectMapper().valueToTree(diagramState), null);
                }
            })
            .match(JsonNode.class, j -> j.has("message"), j -> {

                OnlineNotification notification = new ObjectMapper().treeToValue((JsonNode) j, OnlineNotification.class);
                System.out.println("Online notification received.");

                // todo: send online notification to client
            })
            .matchAny(j -> {

                unhandled(j);
            })
            .build()
        );
    }

    @Override
    public void preStart()
    {
        System.out.println("Pre-Starting Service Actor");
    }

    @Override
    public void postStop() throws Exception
    {
        System.out.println("Pre-Stopping Service Actor");
    }
}
