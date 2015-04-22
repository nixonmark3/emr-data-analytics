package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.processes.AnalyticsProcessBuilder;
import emr.analytics.service.processes.AnalyticsProcessBuilderFactory;

public class JobWorkerActor extends AbstractActor {

    ActorRef _requestor;

    public static Props props(ActorRef requestor) {
        return Props.create(JobWorkerActor.class, requestor);
    }

    public JobWorkerActor(ActorRef requestor){

        _requestor = requestor;

        receive(ReceiveBuilder
            .match(AnalyticsJob.class, job -> {

                // create an actor that tracks the status of the job
                ActorRef jobStatusActor = context().actorOf(
                        JobStatusActor.props(job.getLogLevel(), _requestor), "JobStatusActor");
                // create the actor that actually executes the job
                ActorRef jobExecutionActor = context().actorOf(
                    JobExecutionActor.props(job.getId(), jobStatusActor), "JobExecutionActor");

                AnalyticsProcessBuilder builder = AnalyticsProcessBuilderFactory.get(job);
                jobExecutionActor.tell(builder, self());
            }).build()
        );
    }
}

