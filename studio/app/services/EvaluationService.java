package services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.typesafe.config.ConfigFactory;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.service.JobClientActor;
import emr.analytics.service.jobs.JobMode;
import emr.analytics.service.messages.JobKillRequest;
import emr.analytics.service.messages.JobRequest;

import java.util.UUID;

public class EvaluationService {

    ActorRef client;

    public EvaluationService(){

        final ActorSystem system = ActorSystem.create("job-client-system", ConfigFactory.load("client"));
        final String path = "akka.tcp://job-service-system@127.0.0.1:2552/user/job-service";
        client = system.actorOf(JobClientActor.props(path), "job-client");
    }

    public UUID sendRequest(JobMode mode, Diagram diagram){

        // create the job request
        JobRequest request = new JobRequest(mode, diagram);

        // pass it to the job request actor
        client.tell(request, null);

        return request.getJobId();
    }

    public boolean sendKillRequest(UUID jobId){

        // pass it to the job request actor
        client.tell((new JobKillRequest(jobId)), null);

        return true;
    }
}
