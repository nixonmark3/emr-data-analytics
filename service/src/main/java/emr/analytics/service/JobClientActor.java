package emr.analytics.service;

import static java.util.concurrent.TimeUnit.SECONDS;
import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.messages.BlockStatus;
import emr.analytics.models.messages.EvaluationStatus;
import emr.analytics.models.messages.OnlineNotification;
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.messages.*;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JobClientActor extends AbstractActor {

    private String _path;
    private ActorRef _service = null;
    private ActorRef _compiler;
    private PartialFunction<Object, BoxedUnit> active;
    private ServiceSocketCallback _socketCallback;

    public static Props props(String path, HashMap<String, Definition> definitions){ return Props.create(JobClientActor.class, path, definitions); }

    public JobClientActor(String path, HashMap<String, Definition> definitions) {

        _path = path;
        _compiler = context().actorOf(JobCompilationActor.props(definitions), "job-compiler");
        _socketCallback = new ServiceSocketCallback();

        receive(ReceiveBuilder.
            match(ActorIdentity.class, identity -> {

                _service = ((ActorIdentity) identity).getRef();
                if (_service == null) {
                    System.out.println("Remote actor not available: " + path);
                } else {
                    context().watch(_service);
                    context().become(active, true);
                }
            })
            .match(ReceiveTimeout.class, timeout -> {

                sendIdentifyRequest();
            })
            .matchAny(other -> {

            }).build()
        );

        active = ReceiveBuilder
            .match(JobRequest.class, request -> {

                _compiler.tell(request, self());
            })
            .match(JobKillRequest.class, request -> {

                _service.tell(request, self());
            })
            .match(AnalyticsJob.class, job -> {

                _service.tell(job, self());
            })
            .match(JobStarted.class, status -> {

                if (status.getMode() == Mode.ONLINE)
                    sendOnlineNotification(status.getJobId(), 0, "Job has been started.");
            })
            .match(JobCompleted.class, status -> {

                if (status.getMode() == Mode.ONLINE)
                    sendOnlineNotification(status.getJobId(), 1, "Job has been completed.");
                else
                    sendEvaluationStatus(status.getJobId(), 1);
            })
            .match(JobFailed.class, status -> {

                if (status.getMode() == Mode.ONLINE)
                    sendOnlineNotification(status.getJobId(), 2, "Job has failed.");
                else
                    sendEvaluationStatus(status.getJobId(), 2);
            })
            .match(JobStopped.class, status -> {

            })
            .match(JobProgress.class, status -> {

                // capture progress message
                String message = status.getProgressMessage();

                if (status.getMode() == Mode.ONLINE) {
                    sendOnlineNotification(status.getJobId(), 0, message);
                } else {
                    String[] data = message.split(",");

                    if (data.length == 2) {

                        // if message matches the pattern of a block update - send
                        sendEvaluationStatusForBlock(status.getJobId(), UUID.fromString(data[0]), Integer.valueOf(data[1]));
                    }
                }
            })
            .match(Terminated.class, terminated -> {
                System.out.println("Job Server Terminated");
                sendIdentifyRequest();
                getContext().unbecome();
            })
            .match(ReceiveTimeout.class, timeout -> {
                // ignore
            })
            .matchAny(other -> {
                unhandled(other);
            })
            .build();

        sendIdentifyRequest();
    }

    private void sendIdentifyRequest() {
        getContext().actorSelection(_path).tell(new Identify(_path), self());
        getContext().system().scheduler()
            .scheduleOnce(Duration.create(3, SECONDS), self(),
                    ReceiveTimeout.getInstance(), getContext().dispatcher(), self());
    }

    private void sendEvaluationStatusForBlock(UUID id, UUID blockId, Integer state) {

        BlockStatus blockStatus = new BlockStatus(blockId, state);
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

    private void sendOnlineNotification(UUID id, int state, String message){

        OnlineNotification notification = new OnlineNotification(id, state, message);
        _socketCallback.sendMessage(new ObjectMapper().valueToTree(notification));
    }
}

