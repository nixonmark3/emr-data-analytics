package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.service.jobs.SparkJob;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.StreamingContext;
import scala.PartialFunction;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SparkActor extends AbstractActor {

    private ActorRef client;
    private ActorRef parent;
    private ActorRef statusActor;
    private ActorRef executionActor;
    private SparkJob job;
    private SparkContext sparkContext;
    private StreamingContext streamingContext;
    private PartialFunction<Object, BoxedUnit> active;
    private String parentPath;
    String[] jars = new String[] {
        "../utilities/spark-algorithms/target/scala-2.10/spark-algorithms_2.10-1.0.jar"
    };

    public static Props props(String parentPath) {
        return Props.create(SparkActor.class, parentPath);
    }

    public SparkActor(String parentPath){

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

            .match(ActorRef.class, actor -> {

                this.client = actor;
            })

            .match(SparkJob.class, job -> {

                this.job = job;

                // create the spark context
                SparkConf conf = new SparkConf()
                        .setMaster("local[2]")
                        .setAppName(this.job.getDiagramName().replace(" ", ""))
                        .setJars(jars);
                sparkContext = new SparkContext(conf);
                streamingContext = new StreamingContext(sparkContext, Durations.seconds(1));

                // create the actor that manages the job status
                statusActor = context().actorOf(JobStatusActor.props(self(), this.client, this.job), "JobStatusActor");

                // create the actor that actually executes the job
                executionActor = context().actorOf(SparkExecutionActor.props(statusActor, streamingContext), "JobExecutionActor");

                // send job to executor
                executionActor.tell(job, self());
            })

            /**
             * prematurely initiate the stopping of this job
             */
            .match(String.class, s -> s.equals("stop"), s -> {

                // notify the job status actor that the job is being stopped
                statusActor.tell(new JobStatus(job.getId(), JobStatusTypes.STOPPED), self());
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

                System.out.println("finalizing spark process.");

                streamingContext.stop(true, true);

                getContext().system().shutdown();
            })
            .build();

        // send an identify request to the new spark actor
        sendIdentifyRequest();
    }

    private void sendIdentifyRequest() {
        getContext().actorSelection(this.parentPath).tell(new Identify(this.parentPath), self());
        getContext().system().scheduler()
                .scheduleOnce(Duration.create(3, SECONDS), self(),
                        ReceiveTimeout.getInstance(), getContext().dispatcher(), self());
    }
}
