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

public class SparkWorker extends ExecutionActor implements InterpreterNotificationHandler {

    private ActorRef parent;
    private String parentPath;
    private PartialFunction<Object, BoxedUnit> active;

    public static Props props(String parentPath, UUID diagramId, String diagramName, TargetEnvironments targetEnvironment, Mode mode) {
        return Props.create(SparkWorker.class, parentPath, diagramId, diagramName, targetEnvironment, mode);
    }

    public SparkWorker(String parentPath, UUID diagramId, String diagramName, TargetEnvironments targetEnvironment, Mode mode){
        super(diagramId, diagramName, targetEnvironment, mode);
        this.parentPath = parentPath;

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
            .match(ActorRef.class, this::createStatusManager)

            /**
             * Task request received
             */
            .match(TaskRequest.class, request -> {

                this.runTask(request, request.getSource());
            })

            /**
             * synchronously forward a request to the spark status manager for a summary of the running task
             */
            .match(String.class, s -> s.equals("task"), s -> {

                Timeout timeout = new Timeout(Duration.create(20, TimeUnit.SECONDS));
                Future<Object> future = Patterns.ask(statusManager, s, timeout);
                AnalyticsTask task = (AnalyticsTask) Await.result(future, timeout.duration());

                sender().tell(task, self());
            })

            /**
             * stop interpreter and context
             */
            .match(String.class, s -> s.equals("finalize"), s -> {

                interpreter.stop();
                getContext().system().shutdown();
            })

            /**
             * request to stop this worker, pass a Task Terinated message to the status manager
             */
            .match(String.class, s -> s.equals("stop"), s -> {
                this.terminateTask();
            })

            .build();

        // send an identify request to the new spark actor
        sendIdentifyRequest();
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
}
