package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.jobs.JobMode;
import emr.analytics.service.jobs.PythonJob;
import emr.analytics.service.jobs.SparkStreamingJob;
import emr.analytics.service.messages.JobRequest;

import java.util.Arrays;

public class JobCompilationActor extends AbstractActor {

    public static Props props(){ return Props.create(JobCompilationActor.class); }

    public JobCompilationActor(){

        receive(ReceiveBuilder.
            match(JobRequest.class, request -> {

                // transform request into analytics job
                Diagram diagram = request.getDiagram();

                AnalyticsJob job;
                switch(request.getJobMode()){
                    case Online:
                        job = new SparkStreamingJob(request.getJobId(),
                            JobMode.Online,
                            diagram);
                        break;
                    default:
                        job = new PythonJob(request.getJobId(),
                            JobMode.Offline,
                            diagram);
                        break;
                }

                sender().tell(job, self());
            }).build()
        );
    }
}
