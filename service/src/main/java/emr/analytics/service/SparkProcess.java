package emr.analytics.service;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;

import java.util.Properties;
import java.util.UUID;

public class SparkProcess {

    public static void main(String[] args){
        String id = args[0];
        String system = args[1];
        String host = args[2];
        String port = args[3];
        UUID diagramId = UUID.fromString(args[4]);
        String diagramName = args[5];
        TargetEnvironments targetEnvironment = TargetEnvironments.valueOf(args[6]);
        Mode mode = Mode.valueOf(args[7]);

        String path = String.format("akka.tcp://%s@%s:%s/user/task-service/%s", system, host, port, id);

        final ActorSystem actorSystem = ActorSystem.create("spark-process-system", ConfigFactory.load("spark"));
        actorSystem.actorOf(Props.create(SparkWorker.class, path, diagramId, diagramName, targetEnvironment, mode), "spark-worker");
    }
}
