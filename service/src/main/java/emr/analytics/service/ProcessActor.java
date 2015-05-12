package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.service.jobs.ProcessJob;
import emr.analytics.service.messages.*;

public class ProcessActor extends AbstractActor {

    ActorRef _requestor;
    ActorRef _jobStatusActor;
    ActorRef _jobExecutionActor;
    ProcessJob _job;

    public static Props props(ActorRef requestor, ProcessJob job) {
        return Props.create(ProcessActor.class, requestor, job);
    }

    public ProcessActor(ActorRef requestor, ProcessJob job){

        // reference the requestor and the job
        _requestor = requestor;
        _job = job;

        // create the actor that manages the job status
        _jobStatusActor = context().actorOf(JobStatusActor.props(job.getLogLevel(), _requestor), "JobStatusActor");

        // create the actor that actually executes the job
        _jobExecutionActor = context().actorOf(ProcessExecutionActor.props(_jobStatusActor), "JobExecutionActor");

        receive(ReceiveBuilder
            .match(String.class, s -> s.equals("start"), s -> {

                // send to job executor
                _jobExecutionActor.tell(_job, self());
            })
            .match(String.class, s -> s.equals("kill"), s -> {

                System.out.println("Kill received.");

                // notify the job status actor that the job is being stopped
                _jobStatusActor.tell(new JobStopped(job.getId(), job.getJobMode()), self());
            })
            .match(String.class, s -> s.equals("finalize"), s -> {

                System.out.println("Finalize received.");

                job.removeSource();

                self().tell(PoisonPill.getInstance(), self());
            })
            .build()
        );
    }
}

