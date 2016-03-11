package actors;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.messages.*;
import play.Configuration;
import scala.PartialFunction;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class AnalyticsClient extends AbstractActor {

    private static final String ANALYTICS_CONFIG = "analytics";
    private static final String ANALYTICS_HOST_NAME = "service.host";

    private String remotePath;
    private ActorRef service = null;
    private PartialFunction<Object, BoxedUnit> active;

    public static Props props(){ return Props.create(AnalyticsClient.class); }

    public AnalyticsClient() {

        String host = this.getAnalyticsConfig(ANALYTICS_HOST_NAME);
        remotePath = String.format("akka.tcp://task-service-system@%s:2552/user/task-service", host);

        /**
         * initial receiver; manages the connection to the analytics server
         */
        receive(ReceiveBuilder

            // the analytics service has responded to this actor's identify request
            .match(ActorIdentity.class, identity -> {

                // capture service's actorRef
                this.service = ((ActorIdentity) identity).getRef();

                if (this.service != null) {

                    // send reference of self to the service
                    this.service.tell(self(), self());

                    // send a successful ping message to all clients
                    SessionManager.getInstance().notifyAll(new PingResponse(null, true));

                    context().watch(this.service);
                    context().become(active, true);
                }
            })

            .match(ReceiveTimeout.class, timeout -> {
                sendIdentifyRequest();
            })

                    // ping request fails
            .match(PingRequest.class, ping -> {
                sender().tell(new PingResponse(ping.getId(), false), self());
            })

            .matchAny(this::unhandled).build()
        );

        active = ReceiveBuilder

            // received streaming information from the server
            .match(StreamingTasks.class, tasks -> {
                SessionManager.getInstance().notifySession(tasks.getSessionId(), tasks);
            })

            /**
             * Forward TaskVariables to the associated diagram subscribers
             */
            .match(TaskVariable.class, variable -> {
                SessionManager.getInstance().notifySubscribers(variable.getDiagramId(), variable);
            })

            /**
             * Forward the task request to the analytics service
             */
            .match(TaskRequest.class, request -> {

                this.service.tell(request, self());
            })

            /**
             * Forward the task data to the session that requested it
             */
            .match(TaskData.class, status -> {

                SessionManager.getInstance().notifySession(status.getSessionId(), status);
            })

            /**
             * Forward the task status to all subscribers
             */
            .match(TaskStatus.class, status -> {

                SessionManager.getInstance().notifySubscribers(status.getDiagramId(), status);
            })

            /**
             * Forward the analytics data to the session that requested it
             */
            .match(AnalyticsData.class, data -> {

                SessionManager.getInstance().notifySession(data.getSessionId(), data);
            })

            /**
             * Forward the analytics description to the session that requested it
             */
            .match(AnalyticsDescribe.class, describe -> {

                SessionManager.getInstance().notifySession(describe.getSessionId(), describe);
            })

            // forward the termination request to the analytics service
            .match(TerminationRequest.class, request -> {

                this.service.tell(request, self());
            })

            // forward streaming source request to the analytics service
            .match(StreamingRequest.class, request -> {

                this.service.tell(request, self());
            })

            // forward request to kill a streaming source to the analytics service
            .match(StreamingTerminationRequest.class, request -> {

                this.service.tell(request, self());
            })

            // forward streaming summary request
            .match(StreamingSummaryRequest.class, request -> {

                this.service.tell(request, self());
            })

            // ping request succeeds
            .match(PingRequest.class, ping -> {
                sender().tell(new PingResponse(ping.getId(), true), self());
            })

            // job summary from the analytics server
/*            .match(JobsSummary.class, summary -> {

                SessionManager.getInstance().notifyDashboards(summary);
            })*/

            // forward all input messages to the analytics service
            .match(InputMessage.class, message -> {

                Timeout duration = new Timeout(Duration.create(20, TimeUnit.SECONDS));
                Future<Object> future = Patterns.ask(this.service, message, duration);
                OutputMessage result = (OutputMessage) Await.result(future, duration.duration());

                sender().tell(result, self());
            })

            .match(Terminated.class, terminated -> {

                // send an unsuccessful ping message to all clients
                SessionManager.getInstance().notifyAll(new PingResponse(null, false));

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

    private String getAnalyticsConfig(String name) {
        return Configuration.root().getConfig(ANALYTICS_CONFIG).getString(name);
    }
}