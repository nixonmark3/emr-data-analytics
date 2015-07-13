package emr.analytics.service.spark;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import emr.analytics.service.SparkActor;

public class SparkProcess {

    public static void main(String[] args){
        String id = args[0];
        String system = args[1];
        String host = args[2];
        String port = args[3];

        String path = String.format("akka.tcp://%s@%s:%s/user/job-service/%s", system, host, port, id);

        final ActorSystem actorSystem = ActorSystem.create("spark-process-system", ConfigFactory.load("spark"));
        actorSystem.actorOf(Props.create(SparkActor.class, path), "spark-job");
    }
}
