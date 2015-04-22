package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.messages.BlockStatus;
import emr.analytics.models.messages.EvaluationStatus;
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.messages.*;

import java.util.ArrayList;
import java.util.UUID;

public class JobRequestorActor extends AbstractActor {

    private ActorRef _jobServiceActor;
    private ActorRef _jobCompilationActor;
    private ServiceSocketCallback _socketCallback;

    public static Props props(ActorRef jobServiceActor){ return Props.create(JobRequestorActor.class, jobServiceActor); }

    public JobRequestorActor(ActorRef jobServiceActor){

        _jobServiceActor = jobServiceActor;
        _jobCompilationActor = context().actorOf(JobCompilationActor.props(), "job-compiler");
        _socketCallback = new ServiceSocketCallback();

        receive(ReceiveBuilder.
            match(JobRequest.class, request -> {

                System.out.println("Job request received.");

                _jobCompilationActor.tell(request, self());
            })
            .match(AnalyticsJob.class, job -> {

                System.out.println("Analytics job received from compiler.");

                _jobServiceActor.tell(job, self());
            })
            .match(JobStarted.class, status -> {

            })
            .match(JobCompleted.class, status -> {

                sendEvaluationStatus(status.getJobId(), 1);
            })
            .match(JobFailed.class, status -> {

                sendEvaluationStatus(status.getJobId(), 2);
            })
            .match(JobStopped.class, status -> {

            })
            .match(JobProgress.class, status -> {

                // capture progress message
                String message = status.getProgressMessage();
                String[] data = message.split(",");
                if (data.length == 2) {

                    // if message matches the pattern of a block update - send
                    sendEvaluationStatusForBlock(status.getJobId(), data[0], Integer.valueOf(data[1]));
                }
            }).build()
        );
    }

    private void sendEvaluationStatusForBlock(UUID id, String name, Integer state) {

        BlockStatus blockStatus = new BlockStatus(name, state);
        EvaluationStatus evaluationStatus = new EvaluationStatus(id, 0); // in progress
        ArrayList<BlockStatus> list = new ArrayList<BlockStatus>();
        list.add(blockStatus);
        evaluationStatus.setBlockStatusList(list);
        _socketCallback.sendMessage(new ObjectMapper().valueToTree(evaluationStatus));
    }

    private void sendEvaluationStatus(UUID id, int state) {

        _socketCallback.sendMessage(new ObjectMapper().valueToTree(
            new EvaluationStatus(id, state)));
    }
}

