package services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import emr.analytics.models.diagram.Diagram;
import emr.analytics.service.JobRequestorActor;
import emr.analytics.service.JobServiceActor;
import emr.analytics.service.jobs.LogLevel;
import emr.analytics.service.jobs.TargetEnvironments;
import emr.analytics.service.messages.JobRequest;

import java.util.UUID;

public class EvaluationService {

    ActorSystem system;
    ActorRef service;
    ActorRef requestor;

    public EvaluationService(){

        system = ActorSystem.create("job-service-system");
        service = system.actorOf(JobServiceActor.props(), "job-service");
        requestor = system.actorOf(JobRequestorActor.props(service), "job-requestor");
    }

    public UUID sendRequest(Diagram diagram){

        // create the job request
        JobRequest request = new JobRequest(LogLevel.Progress,
            diagram,
            TargetEnvironments.python);

        // pass it to the job request actor
        requestor.tell(request, null);

        return request.getJobId();
    }
}
