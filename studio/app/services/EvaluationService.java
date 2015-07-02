package services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.ConfigFactory;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.PersistedOutput;
import emr.analytics.service.JobClientActor;
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.messages.JobCompileRequest;
import emr.analytics.service.messages.JobKillRequest;
import emr.analytics.service.messages.JobRequest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EvaluationService {

    ActorRef client;

    public EvaluationService(HashMap<String, Definition> definitions){

        final ActorSystem system = ActorSystem.create("job-client-system", ConfigFactory.load("client"));
        final String path = "akka.tcp://job-service-system@127.0.0.1:2552/user/job-service";
        client = system.actorOf(JobClientActor.props(path, definitions), "job-client");
    }

    public String sendCompileRequest(Diagram diagram){

        HashMap<String, String> outputs = this.getPersistedOutputs(diagram);

        // create the job compile request
        JobCompileRequest request = new JobCompileRequest(new JobRequest(diagram, outputs));

        Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
        Future<Object> future = Patterns.ask(client, request, timeout);

        String result;
        try {
            AnalyticsJob job = (AnalyticsJob) Await.result(future, timeout.duration());
            result = job.getSource();
        }
        catch(java.lang.Exception ex){
            result = "compile exception";
        }

        return result;
    }

    public UUID sendRequest(Diagram diagram){

        HashMap<String, String> outputs = this.getPersistedOutputs(diagram);

        // create the job request
        JobRequest request = new JobRequest(diagram, outputs);

        // pass it to the job request actor
        client.tell(request, null);

        return request.getJobId();
    }

    public boolean sendKillRequest(UUID jobId){

        // pass it to the job request actor
        client.tell((new JobKillRequest(jobId)), null);

        return true;
    }

    private HashMap<String, String> getPersistedOutputs(Diagram diagram){

        HashMap<String, String> persistedOutputs = new HashMap<>();
        for(PersistedOutput persistedOutput : diagram.getPersistedOutputs()){

            BlockResultsService blockResultsService = new BlockResultsService();
            String output = blockResultsService.getOutput(persistedOutput.getId(), persistedOutput.getName());
            String variableName = String.format("%s_%s", persistedOutput.getId(), persistedOutput.getName());

            persistedOutputs.put(variableName, output);
        }

        return persistedOutputs;
    }
}
