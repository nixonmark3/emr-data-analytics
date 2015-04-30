package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.service.jobs.AnalyticsJob;

import java.util.HashMap;
import java.util.Map;

public class JobServiceActor extends AbstractActor {

    // track ids by diagram name
    private Map<String, String> idsByDiagram = new HashMap<>();
    // track workers by unique job name - only 1 can be running at any given time
    private Map<String, ActorRef> workers = new HashMap<>();

    public static Props props(){ return Props.create(JobServiceActor.class); }

    public JobServiceActor(){

        receive(ReceiveBuilder.
            match(AnalyticsJob.class, job -> {

                // reference the job name
                String jobName = job.getName();
                String jobId = job.getId().toString();

                if (workers.containsKey(jobName)) {

                    ActorRef prevJobActor = workers.get(jobName);
                    prevJobActor.tell("kill", self());
                }

                ActorRef jobActor = context().actorOf(JobWorkerActor.props(sender(), job),
                        job.getId().toString());
                idsByDiagram.put(jobId, jobName);
                workers.put(jobName, jobActor);

                context().watch(jobActor);

                jobActor.tell("start", self());

            })
            .match(Terminated.class, t -> {

                String jobId = t.actor().path().name();
                String jobName = idsByDiagram.get(jobId);

                System.out.println(String.format("Job for diagram '%s' has been terminated.",
                        jobName));

                workers.remove(jobName);
                idsByDiagram.remove(jobId);
            })
            .build()
        );
    }
}
