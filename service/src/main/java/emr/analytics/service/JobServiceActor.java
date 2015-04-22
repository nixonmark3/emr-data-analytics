package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.service.jobs.AnalyticsJob;

public class JobServiceActor extends AbstractActor {

    public static Props props(){ return Props.create(JobServiceActor.class); }

    public JobServiceActor(){

        receive(ReceiveBuilder.
            match(AnalyticsJob.class, job -> {

                // reference the job name
                String jobName = job.getName();

                // create job actor
                ActorRef jobActor = context().actorOf(JobWorkerActor.props(sender()), jobName);
                jobActor.tell(job, self());
            }).build()
        );
    }
}
