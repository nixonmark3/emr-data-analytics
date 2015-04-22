package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.service.jobs.LogLevel;
import emr.analytics.service.messages.*;

import java.util.UUID;

/*
 * This actor tracks the status of its assigned job
 */
public class JobStatusActor extends AbstractActor {

    private LogLevel _logLevel;
    private ActorRef _requestor;

    public static Props props(LogLevel logLevel, ActorRef requestor) {
        return Props.create(JobStatusActor.class, logLevel, requestor);
    }

    public JobStatusActor(LogLevel logLevel, ActorRef requestor){

        _logLevel = logLevel;
        _requestor = requestor;

        receive(ReceiveBuilder
            .match(JobStarted.class, status -> {

                if ((_logLevel == LogLevel.All) || (_logLevel == LogLevel.Progress))
                    _requestor.tell(status, self());

            })
            .match(JobCompleted.class, status -> {

                if ((_logLevel == LogLevel.All) || (_logLevel == LogLevel.Progress))
                    _requestor.tell(status, self());

            })
            .match(JobFailed.class, status -> {

                if ((_logLevel == LogLevel.All) || (_logLevel == LogLevel.Progress))
                    _requestor.tell(status, self());
            })
            .match(JobStopped.class, status -> {

                if (_logLevel == LogLevel.All)
                    _requestor.tell(status, self());
            })
            .match(JobProgress.class, status -> {

                if ((_logLevel == LogLevel.All) || (_logLevel == LogLevel.Progress))
                    _requestor.tell(status, self());

            }).build()
        );
    }
}
