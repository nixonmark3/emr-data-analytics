package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.messages.AnalyticsData;
import emr.analytics.models.messages.AnalyticsDescribe;
import emr.analytics.models.messages.AnalyticsTask;
import emr.analytics.models.messages.TaskStatus;

import java.util.UUID;

/**
 * Tracks a jobs current state, forwarding each update to the configured client
 */
public class TaskStatusManager extends AbstractActor {

    // reference to the job actor parent
    private final ActorRef parent;
    // reference to the client actor that requested the job
    private final ActorRef client;
    // current job info
    private final AnalyticsTask task;
    // define the maximum number of task statuses to cache
    private final int statusCapacity = 50;


    public static Props props(ActorRef parent, ActorRef client, UUID diagramId, String diagramName, Mode mode) {
        return Props.create(TaskStatusManager.class, parent, client, diagramId, diagramName, mode);
    }

    public TaskStatusManager(ActorRef parent, ActorRef client, UUID diagramId, String diagramName, Mode mode){

        this.parent = parent;
        this.client = client;
        this.task = new AnalyticsTask(diagramId, diagramName, mode, statusCapacity);

        receive(ReceiveBuilder

            .match(TaskStatus.class, status -> {
                this.task.addStatus(status);
                this.client.tell(status, self());
            })

            .match(AnalyticsDescribe.class, describe -> {
                this.client.tell(describe, self());
            })

            .match(AnalyticsData.class, data -> {
                this.client.tell(data, self());
            })

            .match(String.class, s -> s.equals("task"), s -> {

                sender().tell(task.copy(), self());
            }).build()
        );
    }
}
