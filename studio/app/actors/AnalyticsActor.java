package actors;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.messages.*;
import models.project.DeploymentStatus;
import models.project.EvaluationStatus;
import scala.PartialFunction;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class AnalyticsActor extends AbstractActor {

    private String remotePath = "akka.tcp://job-service-system@127.0.0.1:2552/user/job-service";
    private ActorRef service = null;
    private PartialFunction<Object, BoxedUnit> active;

    public static Props props(){ return Props.create(AnalyticsActor.class); }

    public AnalyticsActor() {

        receive(ReceiveBuilder

            // the analytics service has responded to this actor's identify request
            .match(ActorIdentity.class, identity -> {

                // capture service's actorRef
                this.service = ((ActorIdentity) identity).getRef();

                if (this.service != null) {

                    // send reference of self to the service
                    this.service.tell(self(), self());

                    // send a successful ping message to all clients
                    SessionManager.getInstance().notifyAll(new Ping(true));

                    context().watch(this.service);
                    context().become(active, true);
                }
            })

            .match(ReceiveTimeout.class, timeout -> {
                sendIdentifyRequest();
            })

            // ping request fails
            .match(Ping.class, ping -> {
                ping.setValue(false);
                sender().tell(ping, self());
            })

            .matchAny(this::unhandled).build()
        );

        active = ReceiveBuilder

            // received job information from the server
            .match(JobInfo.class, info -> {

                if (info.getMode() == Mode.OFFLINE){
                    // for offline diagrams - send evaluation status updates
                    SessionManager.getInstance().notifySubscribers(
                            info.getDiagramId(),
                            new EvaluationStatus(info));
                }
                else{
                    // online diagrams - send deployment status updates
                    SessionManager.getInstance().notifySubscribers(
                            info.getDiagramId(),
                            new DeploymentStatus(info));
                }
            })

            // forward job request to the analytics service
            .match(JobRequest.class, request -> {

                this.service.tell(request, self());
            })

            // forward job kill request to the analytics service
            .match(JobKillRequest.class, request -> {

                this.service.tell(request, self());
            })

            // forward streaming source request to the analytics service
            .match(StreamingSourceRequest.class, request -> {

                this.service.tell(request, self());
            })

            // forward request to kill a streaming source to the analytics service
            .match(StreamingSourceKillRequest.class, request -> {

                this.service.tell(request, self());
            })

            // ping request succeeds
            .match(Ping.class, ping -> {
                ping.setValue(true);
                sender().tell(ping, self());
            })

            // job summary from the analytics server
            .match(JobsSummary.class, summary -> {

                SessionManager.getInstance().notifyDashboards(summary);
            })

            // forward all base messages to the analytics service
            .match(BaseMessage.class, message -> {

                Timeout duration = new Timeout(Duration.create(20, TimeUnit.SECONDS));
                Future<Object> future = Patterns.ask(this.service, message, duration);
                BaseMessage result = (BaseMessage) Await.result(future, duration.duration());

                sender().tell(result, self());
            })

            .match(Terminated.class, terminated -> {

                // send an unsuccessful ping message to all clients
                SessionManager.getInstance().notifyAll(new Ping(false));

                sendIdentifyRequest();
                getContext().unbecome();
            })

            .match(ReceiveTimeout.class, timeout -> {
                // ignore
            })

            .matchAny(this::unhandled)

            .build();

        sendIdentifyRequest();
    }

    private void sendIdentifyRequest() {
        getContext().actorSelection(remotePath).tell(new Identify(remotePath), self());
        getContext().system().scheduler()
                .scheduleOnce(Duration.create(1, SECONDS), self(),
                        ReceiveTimeout.getInstance(), getContext().dispatcher(), self());
    }
}