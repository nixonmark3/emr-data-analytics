package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.service.interpreters.*;
import emr.analytics.service.jobs.SparkJob;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;
import scala.PartialFunction;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SparkActor extends AbstractActor implements InterpreterNotificationHandler {

    private ActorRef client;
    private ActorRef parent;
    private ActorRef statusActor;

    private String parentPath;
    private TargetEnvironments targetEnvironment;
    private Mode mode;

    private Interpreter interpreter;

    private PartialFunction<Object, BoxedUnit> active;

    public static Props props(String parentPath, String diagramName, String targetEnvironment, String mode) {
        return Props.create(SparkActor.class, parentPath, diagramName, targetEnvironment, mode);
    }

    public SparkActor(String parentPath, String diagramName, String targetEnvironment, String mode){

        this.parentPath = parentPath;
        this.targetEnvironment = TargetEnvironments.valueOf(targetEnvironment);
        this.mode = Mode.valueOf(mode);

        interpreter = InterpreterFactory.get(diagramName,
                this,
                this.targetEnvironment,
                this.mode);
        interpreter.start();

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

            .match(ActorRef.class, actor -> {

                this.client = actor;
            })

            .match(SparkJob.class, job -> {

                // create the actor that manages the job status
                statusActor = context().actorOf(JobStatusActor.props(self(), this.client, job), "JobStatusActor");

                // report that the job has been started
                statusActor.tell(new JobStatus(JobStatusTypes.STARTED), self());

                // run the job's source code
                this.run(job.getSource());
            })

            /**
             * prematurely initiate the stopping of this job
             */
            .match(String.class, s -> s.equals("stop"), s -> {

                // notify the job status actor that the job is being stopped
                statusActor.tell(new JobStatus(JobStatusTypes.STOPPED), self());
            })

            /**
             * forward the info request to the status actor
             */
            .match(String.class, s -> s.equals("info"), s -> {

                Timeout timeout = new Timeout(Duration.create(20, TimeUnit.SECONDS));
                Future<Object> future = Patterns.ask(statusActor, "info", timeout);
                JobInfo jobInfo = (JobInfo) Await.result(future, timeout.duration());

                sender().tell(jobInfo, self());
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

    public void send(InterpreterNotification notification){

        System.out.println(notification.toString());
    }

    private void sendIdentifyRequest() {
        getContext().actorSelection(this.parentPath).tell(new Identify(this.parentPath), self());
        getContext().system().scheduler()
                .scheduleOnce(Duration.create(3, SECONDS), self(),
                        ReceiveTimeout.getInstance(), getContext().dispatcher(), self());
    }

    private void run(String source){
        new Thread(() -> {

            InterpreterResult result = interpreter.interpret(source);
            System.out.println(String.format("Interpreter Result: %s.", result.toString()));
        }).start();
    }
}
