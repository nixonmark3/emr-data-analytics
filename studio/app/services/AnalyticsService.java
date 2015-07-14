package services;

import actors.AnalyticsActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.ConfigFactory;
import emr.analytics.models.messages.BaseMessage;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class AnalyticsService {

    private static AnalyticsService _instance;
    private ActorRef client;

    public AnalyticsService(){

        ActorSystem system = ActorSystem.create("analytics-client-system", ConfigFactory.load("analytics"));
        client = system.actorOf(AnalyticsActor.props(), "analytics");
    }

    public static AnalyticsService getInstance() {

        if(_instance == null) {
            synchronized (AnalyticsService.class) {

                if (_instance == null)
                    _instance = new AnalyticsService();
            }
        }
        return _instance;
    }

    /**
     *
     * @param message
     * @param actor
     */
    public void send(BaseMessage message, ActorRef actor){
        client.tell(message, actor);
    }

    /**
     *
     * @param message
     */
    public void send(BaseMessage message) { this.send(message, null); }

    /**
     *
     * @param message
     * @param timeout
     * @return
     * @throws Exception
     */
    public BaseMessage sendSync(BaseMessage message, int timeout) throws Exception {
        Timeout duration = new Timeout(Duration.create(timeout, TimeUnit.SECONDS));
        Future<Object> future = Patterns.ask(client, message, duration);

        return (BaseMessage) Await.result(future, duration.duration());
    }

    /**
     *
     * @param message
     * @return
     * @throws Exception
     */
    public BaseMessage sendSync(BaseMessage message) throws Exception {
        return sendSync(message, 5);
    }
}
