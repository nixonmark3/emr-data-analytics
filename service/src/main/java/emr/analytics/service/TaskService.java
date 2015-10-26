package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.messages.*;
import emr.analytics.models.definition.Mode;
import emr.analytics.service.messages.ConsumeJob;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Serves as the primary entry point for executing analytic jobs.  Accepts requests and manages the lifecycle of various
 * job types.
 */
public class TaskService extends AbstractActor {

    // each job is assigned a surrogate uuid (to prevent overlapping actor names)
    private Map<UUID, JobKey> jobKeys = new HashMap<UUID, JobKey>();

    // cache actors by mode {offline, online} and diagram id
    private Map<UUID, ActorRef> offlineWorkers = new HashMap<UUID, ActorRef>();
    private Map<UUID, ActorRef> onlineWorkers = new HashMap<UUID, ActorRef>();

    // cache streaming source actors by their topic
    private Map<String, ActorRef> streamingSources = new HashMap<String, ActorRef>();

    // keep a reference to the remote akka actor client
    private ActorRef client = null;

    // reference to the kafka consumer
    private ActorRef kafkaConsumer;

    public static Props props(String host, String port){ return Props.create(TaskService.class, host, port); }

    public TaskService(String host, String port){

        receive(ReceiveBuilder

            // on remote association the client will pass a reference to itself for reference
            .match(ActorRef.class, actor -> {

                // capture the client reference
                this.client = actor;

                // pass the client to the kafka consumer
                kafkaConsumer.tell(this.client, self());
            })

            // job request sent from client
            .match(TaskRequest.class, request -> {

                // for online jobs that are already running - return
                if (request.getMode() == Mode.ONLINE && this.onlineWorkers.containsKey(request.getDiagramId())) {

                    // notify the sender that this job is already running
                    sender().tell(createFailedMessage(request,
                                    String.format("An online job for diagram, %s, is already running.", request.getDiagramName())),
                            self());
                    return;
                }

                // retrieve / create the job actor
                ActorRef worker;
                if (request.getMode() == Mode.OFFLINE && this.offlineWorkers.containsKey(request.getDiagramId())) {
                    // the offline job is already running - retrieve

                    worker = this.offlineWorkers.get(request.getDiagramId());
                }
                else { // create the job actor based on the target environment specified in the request

                    JobKey jobKey = new JobKey(request.getDiagramId(), request.getMode());

                    switch (request.getTargetEnvironment()) {
                        case PYSPARK:

                            worker = context().actorOf(SparkWorker.props(sender(),
                                            jobKey.getId(),
                                            request.getDiagramId(),
                                            request.getDiagramName(),
                                            request.getTargetEnvironment(),
                                            request.getMode(),
                                            host,
                                            port),
                                    jobKey.getId().toString());

                            break;
                        default:

                            // todo: add support for python

                            // notify the sender that the specified job type is not support
                            sender().tell(createFailedMessage(request, "The job type specified is not currently supported."), self());
                            return;
                    }

                    // store job actor in the appropriate hashmap
                    if (request.getMode() == Mode.OFFLINE) {
                        offlineWorkers.put(request.getDiagramId(), worker);
                    } else if (request.getMode() == Mode.ONLINE) {
                        onlineWorkers.put(request.getDiagramId(), worker);
                        // tell the kafka consumer that a spark job is starting
                        kafkaConsumer.tell(new ConsumeJob(request.getDiagramId(),
                                request.getMetaData(),
                                ConsumeJob.ConsumeState.START), self());
                    }

                    // add new jobkey to the hashmap
                    jobKeys.put(jobKey.getId(), jobKey);

                    // watch for the job actor's termination
                    context().watch(worker);

                    // proactively send an update jobs summary
                    sendTaskCounts(sender());
                }

                // send this job to the job actor
                worker.tell(request, self());
            })

/*            .match(JobKillRequest.class, request -> {

                if (request.getMode() == Mode.OFFLINE && this.offlineWorkers.containsKey(request.getDiagramId())) {

                    // send kill message
                    ActorRef jobActor = this.offlineWorkers.get(request.getDiagramId());
                    jobActor.tell("stop", self());
                } else if (request.getMode() == Mode.ONLINE && this.onlineWorkers.containsKey(request.getDiagramId())) {

                    // send kill message
                    ActorRef jobActor = this.onlineWorkers.get(request.getDiagramId());
                    jobActor.tell("stop", self());
                }
            })*/

/*            .match(StreamingSourceRequest.class, request -> {

                if (this.streamingSources.containsKey(request.getTopic())) {

                    // todo: notify the sender that this topic is already running
                    return;
                }

                // todo: check whether kafka is running

                // initialize, cache, and start kafka actor
                ActorRef kafkaActor = context().actorOf(KafkaProducer.props(request, this.client), request.getTopic());
                streamingSources.put(request.getTopic(), kafkaActor);
                context().watch(kafkaActor);

                kafkaActor.tell("start", self());

                // proactively send an update jobs summary
                sendJobsSummary(sender());
            })

            .match(StreamingSourceKillRequest.class, request -> {

                if (this.streamingSources.containsKey(request.getTopic())) {

                    // send kill message
                    ActorRef jobActor = this.streamingSources.get(request.getTopic());
                    jobActor.tell("stop", self());
                }
            })*/

            .match(TaskSummaryRequest.class, request -> {

                AnalyticsTasks tasks = new AnalyticsTasks();

                if (this.offlineWorkers.containsKey(request.getDiagramId())) {

                    ActorRef jobActor = this.offlineWorkers.get(request.getDiagramId());
                    AnalyticsTask task = (AnalyticsTask) getTask(jobActor);
                    if (task != null)
                        tasks.add(task);
                }

                if (this.onlineWorkers.containsKey(request.getDiagramId())) {

                    ActorRef jobActor = this.onlineWorkers.get(request.getDiagramId());
                    AnalyticsTask task = (AnalyticsTask) getTask(jobActor);
                    if (task != null)
                        tasks.add(task);
                }

                sender().tell(tasks, self());
            })

            /*.match(BaseMessage.class, message -> message.getType().equals("offline-jobs"), message -> {

                // build a list of current jobs
                JobInfos jobs = new JobInfos();
                for (ActorRef worker : this.offlineWorkers.values()) {

                    JobInfo job = (JobInfo)getInfo(worker);
                    if (job != null)
                        jobs.add(job);
                }

                sender().tell(jobs, self());
            })

            .match(BaseMessage.class, message -> message.getType().equals("online-jobs"), message -> {

                // build a list of current jobs
                JobInfos jobs = new JobInfos();
                for (ActorRef worker : this.onlineWorkers.values()) {

                    JobInfo job = (JobInfo)getInfo(worker);
                    if (job != null)
                        jobs.add(job);
                }

                sender().tell(jobs, self());
            })

            .match(BaseMessage.class, message -> message.getType().equals("streaming-jobs"), message -> {

                // build a list of current jobs
                StreamingInfos jobs = new StreamingInfos();
                for (ActorRef worker : this.streamingSources.values()) {

                    StreamingInfo job = (StreamingInfo)getInfo(worker);
                    if (job != null)
                        jobs.add(job);
                }

                sender().tell(jobs, self());
            })

            .match(BaseMessage.class, message -> message.getType().equals("jobs-summary"), message -> {
                sendJobsSummary(sender());
            })*/

            .match(Terminated.class, t -> {

                // clean up terminated actors

                String actorName = t.actor().path().name();

                if (isUUID(actorName)) {
                    // if the job name is a uuid - clean up completed jobs

                    UUID jobId = UUID.fromString(actorName);
                    JobKey jobKey = jobKeys.get(jobId);
                    if (jobKey.getMode() == Mode.OFFLINE && this.offlineWorkers.containsKey(jobKey.getDiagramId())) {
                        this.offlineWorkers.remove(jobKey.getDiagramId());
                    } else if (jobKey.getMode() == Mode.ONLINE && this.onlineWorkers.containsKey(jobKey.getDiagramId())) {
                        this.onlineWorkers.remove(jobKey.getDiagramId());

                        // tell the kafka consumer that a spark job has been terminated
                        kafkaConsumer.tell(new ConsumeJob(jobKey.getDiagramId(), "", ConsumeJob.ConsumeState.END), self());
                    }

                    jobKeys.remove(jobId);
                } else if (this.streamingSources.containsKey(actorName)) {

                    // clean up the streaming source data actor
                    this.streamingSources.remove(actorName);
                }

                if (this.client != null)
                    sendTaskCounts(this.client);

            }).build()
        );

        kafkaConsumer = context().actorOf(KafkaConsumer.props());
    }

    //
    // private methods
    //

    /**
     *
     * @param request
     * @param message
     * @return
     */
    private TaskFailed createFailedMessage(TaskRequest request, String message){

        TaskFailed status = new TaskFailed(request.getDiagramId(),
                request.getDiagramName(),
                request.getMode(),
                message);

        return status;
    }

    /**
     * Given a job actor reference, synchronously retrieve the job info
     * @param actor: Job Actor reference
     * @return jobInfo or null (on exception)
     */
    private Object getTask(ActorRef actor) {

        try {
            // define a 10 second timeout
            Timeout timeout = new Timeout(Duration.create(20, TimeUnit.SECONDS));
            // synchronously pass info request
            Future<Object> future = Patterns.ask(actor, "task", timeout);
            return Await.result(future, timeout.duration());
        }
        catch(Exception ex){    // return null on exception
            return null;
        }
    }

    /**
     * apply a regex to determine whether a string is a uuid
     * @param data: string
     * @return boolean
     */
    private boolean isUUID(String data){
        return (data.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"));
    }

    /**
     * Send the current jobs summary to the specified client
     * @param client
     */
    private void sendTaskCounts(ActorRef client){

        TaskCounts counts = new TaskCounts(this.offlineWorkers.size(),
                this.onlineWorkers.size(),
                this.streamingSources.size());

        client.tell(counts, self());
    }

    /**
     * class that tracks the essential details of a job
     */
    private class JobKey{

        private UUID id;
        private UUID diagramId;
        private Mode mode;

        public JobKey(UUID diagramId, Mode mode){
            this.id = UUID.randomUUID();
            this.diagramId = diagramId;
            this.mode = mode;
        }

        public UUID getDiagramId() { return this.diagramId; }

        public UUID getId() { return this.id; }

        public Mode getMode() { return this.mode; }
    }
}