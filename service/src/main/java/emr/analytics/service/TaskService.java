package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.diagram.interpreter.DiagramOperations;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.messages.*;
import emr.analytics.models.definition.Mode;
import emr.analytics.service.messages.ConsumerStart;
import emr.analytics.service.messages.ConsumerStop;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * TaskService Listener: serves as the entry point for execution of analytic tasks
 * Accepts requests and manages the lifecycle of various task types.
 */
public class TaskService extends AbstractActor {

    // each task is assigned a surrogate uuid (to prevent overlapping actor names)
    private Map<UUID, TaskKey> taskKeys = new HashMap<UUID, TaskKey>();

    // cache working actors by mode {offline, online} and diagram id
    private Map<UUID, ActorRef> offlineWorkers = new HashMap<UUID, ActorRef>();
    private Map<UUID, ActorRef> onlineWorkers = new HashMap<UUID, ActorRef>();

    // cache streaming source actors by their topic
    private Map<String, ActorRef> streamingSources = new HashMap<String, ActorRef>();

    // keep a reference to the remote akka actor client
    private ActorRef client = null;

    // kafka consuming actor (listens for updates to online task topic)
    private ActorRef kafkaConsumer;

    public static Props props(String host, String port){ return Props.create(TaskService.class, host, port); }

    /**
     * TaskService constructor
     * @param host: the server's host
     * @param port: the server's port
     */
    public TaskService(String host, String port){

        receive(ReceiveBuilder

            /**
             * on remote association, the client will pass a reference to itself for reference
             */
            .match(ActorRef.class, actor -> {

                // capture the client reference
                this.client = actor;

                // pass the client to the kafka consumer
                kafkaConsumer.tell(this.client, self());
            })

            /**
             * task request sent from client
             */
            .match(TaskRequest.class, request -> {

                // for online tasks that are already running - return
                if (request.getMode() == Mode.ONLINE && this.onlineWorkers.containsKey(request.getDiagramId())) {

                    // notify the sender that this task is already running
                    sender().tell(createFailedMessage(request,
                                    String.format("An online task for diagram, %s, is already running.", request.getDiagramName())),
                            self());
                    return;
                }

                // retrieve / create the appropriate worker
                ActorRef worker;
                if (request.getMode() == Mode.OFFLINE && this.offlineWorkers.containsKey(request.getDiagramId())) {

                    // the offline task is already running - retrieve
                    worker = this.offlineWorkers.get(request.getDiagramId());
                }
                else { // create the working actor based on the target environment specified in the request

                    TaskKey taskKey = new TaskKey(request.getDiagramId(), request.getMode());

                    switch (request.getTargetEnvironment()) {
                        case PYSPARK:

                            worker = context().actorOf(SparkService.props(sender(),
                                            taskKey.getId(),
                                            request.getDiagramId(),
                                            request.getDiagramName(),
                                            request.getTargetEnvironment(),
                                            request.getMode(),
                                            host,
                                            port),
                                    taskKey.getId().toString());

                            break;

                        case PYTHON:

                            worker = context().actorOf(PythonWorker.props(this.client,
                                    request.getDiagramId(),
                                    request.getDiagramName()),
                                    taskKey.getId().toString());

                            break;
                        default:

                            // notify the sender that the specified task type is not support
                            sender().tell(createFailedMessage(request, "The task type specified is not currently supported."), self());
                            return;
                    }

                    // store working actor in the appropriate hashmap
                    if (request.getMode() == Mode.OFFLINE) {

                        offlineWorkers.put(request.getDiagramId(), worker);
                    }
                    else if (request.getMode() == Mode.ONLINE) {

                        onlineWorkers.put(request.getDiagramId(), worker);

                        if(request instanceof DiagramTaskRequest){
                            // for Online DiagramTaskRequests retrieve the configured consumers
                            // and notify the KafkaConsumer

                            Diagram diagram = ((DiagramTaskRequest)request).getDiagram();
                            // Retrieve the output consumer configuration
                            Consumers consumers = DiagramOperations.getOnlineConsumers(diagram);
                            // Pass to the KafkaConsumer
                            kafkaConsumer.tell(new ConsumerStart(request.getId(),
                                    request.getDiagramId(),
                                    consumers), self());
                        }
                    }

                    // add new taskKey to the hash map
                    taskKeys.put(taskKey.getId(), taskKey);

                    // watch for the task actor's termination
                    context().watch(worker);
                }

                // send this job to the job actor
                worker.tell(request, self());
            })

            /**
             * Task termination request
             */
            .match(TerminationRequest.class, request -> {

                if (request.getMode() == Mode.OFFLINE && this.offlineWorkers.containsKey(request.getDiagramId())) {

                    // send termination message
                    ActorRef worker = this.offlineWorkers.get(request.getDiagramId());
                    worker.tell("stop", self());
                }
                else if (request.getMode() == Mode.ONLINE && this.onlineWorkers.containsKey(request.getDiagramId())) {

                    // send termination message
                    ActorRef worker = this.onlineWorkers.get(request.getDiagramId());
                    worker.tell("stop", self());
                }
            })

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

            /**
             *
             */
            .match(TaskSummaryRequest.class, request -> {

                AnalyticsTasks tasks = new AnalyticsTasks(request.getSessionId());

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

            /**
             * A watched actor has been terminated - clean it up
             */
            .match(Terminated.class, t -> {

                // clean up terminated actors

                String actorName = t.actor().path().name();

                if (isUUID(actorName)) {
                    // if the job name is a uuid - clean up completed jobs

                    System.out.println(String.format("Actor terminated: %s.", actorName));

                    UUID jobId = UUID.fromString(actorName);
                    TaskKey taskKey = taskKeys.get(jobId);
                    if (taskKey.getMode() == Mode.OFFLINE && this.offlineWorkers.containsKey(taskKey.getDiagramId())) {
                        this.offlineWorkers.remove(taskKey.getDiagramId());
                    } else if (taskKey.getMode() == Mode.ONLINE && this.onlineWorkers.containsKey(taskKey.getDiagramId())) {
                        this.onlineWorkers.remove(taskKey.getDiagramId());

                        // tell the kafka consumer that a spark job has been terminated
                        kafkaConsumer.tell(new ConsumerStop(taskKey.getDiagramId()), self());
                    }

                    taskKeys.remove(jobId);
                } else if (this.streamingSources.containsKey(actorName)) {

                    // clean up the streaming source data actor
                    this.streamingSources.remove(actorName);
                }

            }).build()
        );

        // create the kafka consumer
        kafkaConsumer = context().actorOf(KafkaConsumer.props());
    }

    //
    // private methods
    //

    /**
     * Create a failed task message
     * @param request: the failed request
     * @param message: the error message
     * @return: Failed task message
     */
    private TaskFailed createFailedMessage(TaskRequest request, String message){

        TaskFailed status = new TaskFailed(
                request.getId(),
                request.getSessionId(),
                request.getDiagramId(),
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
     * class that summarizes the essential details of a task
     */
    private class TaskKey{

        private UUID id;
        private UUID diagramId;
        private Mode mode;

        public TaskKey(UUID diagramId, Mode mode){
            this.id = UUID.randomUUID();
            this.diagramId = diagramId;
            this.mode = mode;
        }

        public UUID getDiagramId() { return this.diagramId; }

        public UUID getId() { return this.id; }

        public Mode getMode() { return this.mode; }
    }
}