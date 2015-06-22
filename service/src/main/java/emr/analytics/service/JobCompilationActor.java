package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.jobs.PythonJob;
import emr.analytics.service.jobs.SparkStreamingJob;
import emr.analytics.service.messages.JobRequest;

import java.util.HashMap;

public class JobCompilationActor extends AbstractActor {

    private HashMap<String, Definition> _definitions;

    public static Props props(HashMap<String, Definition> definitions){ return Props.create(JobCompilationActor.class, definitions); }

    public JobCompilationActor(HashMap<String, Definition> definitions){

        _definitions = definitions;

        receive(ReceiveBuilder.
            match(JobRequest.class, request -> {

                // transform request into analytics job
                Diagram diagram = request.getDiagram();

                AnalyticsJob job;
                switch(request.getMode()){
                    case ONLINE:
                        job = new SparkStreamingJob(request, _definitions);
                        break;
                    default:
                        job = new PythonJob(request, _definitions);
                        break;
                }

                sender().tell(job, self());
            }).build()
        );
    }
}
