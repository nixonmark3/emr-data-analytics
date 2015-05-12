package emr.analytics.service;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

public class JobServer {

    public static void main(String[] args){

        final ActorSystem system = ActorSystem.create("job-service-system",
                ConfigFactory.load(("server")));
        system.actorOf(Props.create(JobServiceActor.class), "job-service");
        System.out.println("Started Job Server");
    }
}
