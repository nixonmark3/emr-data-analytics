package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.DiagramStates;
import controllers.Diagrams;

import emr.analytics.models.messages.EvaluationStatus;
import emr.analytics.models.state.DiagramState;

import java.util.UUID;

public class ServiceActor extends UntypedActor
{
    private final ActorRef out;

    public static Props props(ActorRef out)
    {
        return Props.create(ServiceActor.class, out);
    }

    public ServiceActor(ActorRef out)
    {
        this.out = out;
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

    public void onReceive(Object message) throws Exception
    {
        if (message instanceof JsonNode)
        {
            EvaluationStatus evaluationStatus = new ObjectMapper().treeToValue((JsonNode)message, EvaluationStatus.class);
            System.out.println(evaluationStatus.getJobId() + " - " + evaluationStatus.getState());

            DiagramState diagramState = DiagramStates.getDiagramState(evaluationStatus.getJobId());

            if (diagramState != null)
            {
                diagramState.setState(evaluationStatus.getState());

                if (evaluationStatus.getState() == 0)
                {
                    evaluationStatus.getBlockStatusList().stream()
                            .forEach(b -> diagramState.setBlockState(b.getBlockName(), b.getState()));
                }

                DiagramStates.saveDiagramState(diagramState);

                UUID clientId = Diagrams.getClientIdForJob(diagramState.getJobId());

                ClientActorManager
                        .getInstance()
                        .getClientActor(clientId)
                        .tell(new ObjectMapper().valueToTree(diagramState), null);
            }
        }
        else
        {
            unhandled(message);
        }
    }
}
