package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.service.jobs.ProcessJob;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Manages the execution of process-based jobs
 */
public class ProcessActor extends AbstractActor {

    ActorRef client;
    ActorRef statusActor;
    ActorRef executionActor;
    ProcessJob job;

    public static Props props(ActorRef client, ProcessJob job) {
        return Props.create(ProcessActor.class, client, job);
    }

    public ProcessActor(ActorRef client, ProcessJob job){

        this.client = client;
        this.job = job;

        // create the actor that manages the job's status
        statusActor = context().actorOf(JobStatusActor.props(self(), client, this.job), "JobStatusActor");

        // create the actor that actually executes the job
        executionActor = context().actorOf(ProcessExecutionActor.props(statusActor), "JobExecutionActor");

        receive(ReceiveBuilder

            /**
             * start this job
             */
            .match(String.class, s -> s.equals("start"), s -> {

                // send job to executor
                executionActor.tell(job, self());
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

                Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
                Future<Object> future = Patterns.ask(statusActor, "info", timeout);
                JobInfo jobInfo = (JobInfo) Await.result(future, timeout.duration());

                sender().tell(jobInfo, self());
            })

            /**
             * remove the source file and send self a poison pill
             */
            .match(String.class, s -> s.equals("finalize"), s -> {

                job.removeSource();

                self().tell(PoisonPill.getInstance(), self());
            })
            .build()
        );
    }
}

