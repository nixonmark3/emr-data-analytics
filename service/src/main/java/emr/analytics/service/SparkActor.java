package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.service.jobs.SparkStreamingJob;
import emr.analytics.service.messages.JobStopped;
import org.apache.spark.streaming.StreamingContext;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class SparkActor extends AbstractActor {

    ActorRef _requestor;
    ActorRef _jobStatusActor;
    ActorRef _jobExecutionActor;
    SparkStreamingJob _job;
    StreamingContext _streamingContext = null;

    public static Props props(ActorRef requestor, SparkStreamingJob job) {
        return Props.create(SparkActor.class, requestor, job);
    }

    public SparkActor(ActorRef requestor, SparkStreamingJob job){

        // reference the requestor and the job
        _requestor = requestor;
        _job = job;

        // create the actor that manages the job status
        _jobStatusActor = context().actorOf(JobStatusActor.props(job.getLogLevel(), _requestor), "JobStatusActor");

        // create the actor that actually executes the job
        _jobExecutionActor = context().actorOf(SparkExecutionActor.props(_jobStatusActor), "JobExecutionActor");

        receive(ReceiveBuilder
            .match(String.class, s -> s.equals("start"), s -> {

                // send the job to the execution actor and wait for the spark streaming context to be returned
                Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
                Future<Object> future = Patterns.ask(_jobExecutionActor, _job, timeout);
                _streamingContext = (StreamingContext) Await.result(future, timeout.duration());
            })
            .match(String.class, s -> s.equals("kill"), s -> {

                System.out.println("Kill received.");

                // notify the job status actor that the job is being stopped
                _jobStatusActor.tell(new JobStopped(job.getId(), job.getMode()), self());
            })
            .match(String.class, s -> s.equals("finalize"), s -> {

                System.out.println("Finalize received.");

                _streamingContext.stop(true, true);

                self().tell(PoisonPill.getInstance(), self());
            })
            .build()
        );
    }
}
