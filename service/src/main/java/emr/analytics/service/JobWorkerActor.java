package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.messages.*;
import emr.analytics.service.processes.AnalyticsProcessBuilder;
import emr.analytics.service.processes.AnalyticsProcessBuilderFactory;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class JobWorkerActor extends AbstractActor {

    ActorRef _requestor;
    ActorRef _jobStatusActor;
    ActorRef _jobExecutionActor;
    AnalyticsJob _job;
    AnalyticsProcessBuilder _builder;

    public static Props props(ActorRef requestor, AnalyticsJob job) {
        return Props.create(JobWorkerActor.class, requestor, job);
    }

    public JobWorkerActor(ActorRef requestor, AnalyticsJob job){

        // reference the requestor and the job
        _requestor = requestor;
        _job = job;

        // create the actor that manages the job status
        _jobStatusActor = context().actorOf(JobStatusActor.props(job.getLogLevel(), _requestor), "JobStatusActor");

        // create the actor that actually executes the job
        _jobExecutionActor = context().actorOf(
                JobExecutionActor.props(_job.getId(), job.getJobMode(), _jobStatusActor), "JobExecutionActor");

        // get process builder
        _builder = AnalyticsProcessBuilderFactory.get(job);

        receive(ReceiveBuilder
            .match(String.class, s -> s.equals("start"), s -> {

                // send to job executor
                _jobExecutionActor.tell(_builder, self());
            })
            .match(String.class, s -> s.equals("kill"), s -> {

                System.out.println("Kill received.");

                // notify the job status actor that the job is being stopped
                _jobStatusActor.tell(new JobStopped(job.getId(), job.getJobMode()), self());
            })
            .match(String.class, s -> s.equals("finalize"), s -> {

                System.out.println("Finalize received.");

                Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
                Await.result(Patterns.ask(_jobExecutionActor, "kill", timeout),
                    timeout.duration());

                this.cleanup(_builder.getFileName());

                self().tell(PoisonPill.getInstance(), self());
            })
            .build()
        );
    }

    private void cleanup(String fileName) {
        try {
            Files.delete(Paths.get(fileName));
        }
        catch(IOException ex) {
            System.err.println("IO Exception occurred.");
        }
    }
}

