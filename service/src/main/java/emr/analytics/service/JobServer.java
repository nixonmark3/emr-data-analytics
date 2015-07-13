package emr.analytics.service;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class JobServer {

    public static void main(String[] args){

        Config config = ConfigFactory.load("service");
        String host = config.getString("akka.remote.netty.tcp.hostname");
        String port = config.getString("akka.remote.netty.tcp.port");

        final ActorSystem system = ActorSystem.create("job-service-system", config);
        system.actorOf(Props.create(JobServiceActor.class, host, port), "job-service");
    }
}
