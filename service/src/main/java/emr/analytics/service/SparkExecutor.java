package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.*;
import emr.analytics.service.interpreters.*;
import scala.PartialFunction;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SparkExecutor extends AbstractActor implements InterpreterNotificationHandler {

    private ActorRef client;
    private ActorRef parent;
    private UUID diagramId;
    private String diagramName;
    private Mode mode;
    private ActorRef statusManager;

    private String parentPath;

    private Interpreter interpreter;

    private PartialFunction<Object, BoxedUnit> active;

    public static Props props(String parentPath, UUID diagramId, String diagramName, TargetEnvironments targetEnvironment, Mode mode) {
        return Props.create(SparkExecutor.class, parentPath, diagramId, diagramName, targetEnvironment, mode);
    }

    public SparkExecutor(String parentPath, UUID diagramId, String diagramName, TargetEnvironments targetEnvironment, Mode mode){

        this.parentPath = parentPath;
        this.diagramId = diagramId;
        this.diagramName = diagramName;
        this.mode = mode;

        this.interpreter = InterpreterFactory.get(diagramName,
                this,
                targetEnvironment,
                mode);
        this.interpreter.start();

        receive(ReceiveBuilder

            // the remote spark process has responded to this actor's identify request
            .match(ActorIdentity.class, identity -> {

                // capture service's actorRef
                parent = identity.getRef();

                if (parent != null) {

                    // send self to parent
                    parent.tell(self(), self());

                    context().watch(parent);
                    context().become(active, true);
                }
            })

            // the spark process did not respond - resend the identity request
            .match(ReceiveTimeout.class, timeout -> {

                sendIdentifyRequest();
            })

            .matchAny(this::unhandled).build()
        );

        active = ReceiveBuilder

            /**
             *
             */
            .match(ActorRef.class, actor -> {

                this.client = actor;

                // create the actor that manages the task status
                this.statusManager = context().actorOf(TaskStatusManager.props(self(), this.client, diagramId, diagramName, mode), "TaskStatusManager");
            })

            /**
             * Task request received
             */
            .match(TaskRequest.class, request -> {

                // notify the status manager that a task is starting
                statusManager.tell(new TaskStarted(diagramId, diagramName, mode, request.getSource()), self());

                // run the job's source code
                this.run(request.getSource());
            })

            /**
             * terminate task session
             */
/*            .match(String.class, s -> s.equals("terminate"), s -> {

                // notify the job status actor that the job is being stopped
                statusManager.tell(new JobStatus(JobStatusTypes.STOPPED), self());
            })

            //**
             * forward the info request to the status actor
             */
            .match(String.class, s -> s.equals("task"), s -> {

                Timeout timeout = new Timeout(Duration.create(20, TimeUnit.SECONDS));
                Future<Object> future = Patterns.ask(statusManager, s, timeout);
                AnalyticsTask task = (AnalyticsTask) Await.result(future, timeout.duration());

                sender().tell(task, self());
            })

            /**
             * stop context and system system
             */
            .match(String.class, s -> s.equals("finalize"), s -> {

                interpreter.stop();
                getContext().system().shutdown();
            })
            .build();

        // send an identify request to the new spark actor
        sendIdentifyRequest();
    }

    /**
     *
     * @param notification
     */
    public void notify(InterpreterNotification notification){

        TaskStatus status;
        switch(notification.getKey()){
            case "OUT":
                status = new TaskOutput(diagramId, diagramName, mode, notification.getValue());
                break;
            case "ERROR":
                status = new TaskError(diagramId, diagramName, mode, notification.getValue());
                break;
            default:
                status = new TaskNotification(diagramId, diagramName, mode, notification.getKey(), notification.getValue());
        }

        statusManager.tell(status, self());
    }

    /**
     * Wrap the describe into an AnalyticsDescribe object and associate with the current diagram id
     * @param describe Dataframe statistics summary
     */
    public void describe(Describe describe){
        statusManager.tell(new AnalyticsDescribe(diagramId, describe), self());
    }

    /**
     * Send an identity message to initialize contact with parent
     */
    private void sendIdentifyRequest() {
        getContext().actorSelection(this.parentPath).tell(new Identify(this.parentPath), self());
        getContext().system().scheduler()
                .scheduleOnce(Duration.create(3, SECONDS), self(),
                        ReceiveTimeout.getInstance(), getContext().dispatcher(), self());
    }

    private void run(String source){

        new Thread(() -> {
            InterpreterResult result = interpreter.interpret(source);

            TaskStatus status;
            switch(result.getState()){
                case SUCCESS:
                    status = new TaskCompleted(diagramId, diagramName, mode);
                    statusManager.tell(status, self());
                    break;
                case FAILURE:
                    status = new TaskFailed(diagramId, diagramName, mode, result.getMessage());
                    statusManager.tell(status, self());
                    break;
            }

        }).start();
    }
}
