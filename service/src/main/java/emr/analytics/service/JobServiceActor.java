package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.messages.*;
import emr.analytics.models.definition.Mode;
import emr.analytics.service.jobs.*;
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
public class JobServiceActor extends AbstractActor {

    // map analytic jobs to their ids
    private Map<UUID, AnalyticsJob> analyticJobs = new HashMap<UUID, AnalyticsJob>();

    // cache actors by mode {offline, online} and diagram id
    private Map<UUID, ActorRef> offlineJobs = new HashMap<UUID, ActorRef>();
    private Map<UUID, ActorRef> onlineJobs = new HashMap<UUID, ActorRef>();

    // cache streaming source actors by their topic
    private Map<String, ActorRef> streamingSources = new HashMap<String, ActorRef>();

    // keep a reference to the remote akka actor client
    private ActorRef client = null;

    // reference to the kafka consumer
    private ActorRef kafkaConsumer;

    public static Props props(String host, String port){ return Props.create(JobServiceActor.class, host, port); }

    public JobServiceActor(String host, String port){

        receive(ReceiveBuilder

            // on remote association the client will pass a reference to itself for reference
            .match(ActorRef.class, actor -> {

                // capture the client reference
                this.client = actor;

                // pass the client to the kafka consumer
                kafkaConsumer.tell(this.client, self());
            })

            // job request sent from client
            .match(JobRequest.class, request -> {

                if (request.getMode() == Mode.OFFLINE && this.offlineJobs.containsKey(request.getDiagramId())) {

                    // notify the sender that this job is already running
                    sender().tell(createFailedJobInfo(request,
                                    String.format("This offline job specified, %s, is already running.", request.getDiagramName())),
                            self());

                    return;
                } else if (request.getMode() == Mode.ONLINE && this.onlineJobs.containsKey(request.getDiagramId())) {

                    // notify the sender that this job is already running
                    sender().tell(createFailedJobInfo(request,
                                    String.format("This online job specified, %s, is already running.", request.getDiagramName())),
                            self());

                    return;
                }

                AnalyticsJob job;
                try {
                    job = JobFactory.get(request);
                } catch (AnalyticsJobException ex) {

                    // notify the sender that an exception occurred while creating analytics job
                    sender().tell(createFailedJobInfo(request,
                                    String.format("Failed to transform request into job. Additional info: %s.",
                                            ex.getMessage())),
                            self());
                    return;
                }

                ActorRef jobActor;
                if (job instanceof ProcessJob) {

                    jobActor = context().actorOf(
                            ProcessActor.props(sender(), (ProcessJob) job),
                            job.getId().toString());
                    jobActor.tell("start", self());

                }
                else if (job instanceof SparkJob) {

                    // create a new spark process actor - send job
                    SparkJob sparkJob = (SparkJob) job;

                    jobActor = context().actorOf(SparkProcessActor.props(sender(),
                                    job.getId(),
                                    sparkJob.getDiagramName(),
                                    sparkJob.getTargetEnvironment(),
                                    sparkJob.getMode(),
                                    host,
                                    port),
                            job.getId().toString());

                    jobActor.tell(sparkJob, self());

                    // tell the kafka consumer that a spark job is starting
                    kafkaConsumer.tell(new ConsumeJob(job.getKey().getId(), sparkJob.getMetaData(), ConsumeJob.ConsumeState.START), self());
                }
                else {

                    // notify the sender that the specified job type is not support
                    sender().tell(createFailedJobInfo(request, "The job specified is not supported."), self());
                    return;
                }

                // store job information for reference later
                if (job.getMode() == Mode.OFFLINE)
                    offlineJobs.put(job.getKey().getId(), jobActor);
                else if (job.getMode() == Mode.ONLINE)
                    onlineJobs.put(job.getKey().getId(), jobActor);
                analyticJobs.put(job.getId(), job);
                context().watch(jobActor);

                // proactively send an update jobs summary
                JobsSummary summary = new JobsSummary(this.offlineJobs.size(),
                        this.onlineJobs.size(),
                        this.streamingSources.size());
                sender().tell(summary, self());
            })

            .match(JobKillRequest.class, request -> {

                if (request.getMode() == Mode.OFFLINE && this.offlineJobs.containsKey(request.getDiagramId())) {

                    // send kill message
                    ActorRef jobActor = this.offlineJobs.get(request.getDiagramId());
                    jobActor.tell("stop", self());
                } else if (request.getMode() == Mode.ONLINE && this.onlineJobs.containsKey(request.getDiagramId())) {

                    // send kill message
                    ActorRef jobActor = this.onlineJobs.get(request.getDiagramId());
                    jobActor.tell("stop", self());
                }
            })

            .match(StreamingSourceRequest.class, request -> {

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
                JobsSummary summary = new JobsSummary(this.offlineJobs.size(),
                        this.onlineJobs.size(),
                        this.streamingSources.size());
                sender().tell(summary, self());
            })

            .match(StreamingSourceKillRequest.class, request -> {

                if (this.streamingSources.containsKey(request.getTopic())) {

                    // send kill message
                    ActorRef jobActor = this.streamingSources.get(request.getTopic());
                    jobActor.tell("stop", self());
                }
            })

            .match(JobInfoRequest.class, request -> {

                JobInfos jobs = new JobInfos();

                if (this.offlineJobs.containsKey(request.getDiagramId())) {

                    ActorRef jobActor = this.offlineJobs.get(request.getDiagramId());
                    JobInfo job = (JobInfo)getInfo(jobActor);
                    if (job != null)
                        jobs.add(job);
                }

                if (this.onlineJobs.containsKey(request.getDiagramId())) {

                    ActorRef jobActor = this.onlineJobs.get(request.getDiagramId());
                    JobInfo job = (JobInfo)getInfo(jobActor);
                    if (job != null)
                        jobs.add(job);
                }

                sender().tell(jobs, self());
            })

            .match(BaseMessage.class, message -> message.getType().equals("offline-jobs"), message -> {

                // build a list of current jobs
                JobInfos jobs = new JobInfos();
                for (ActorRef worker : this.offlineJobs.values()) {

                    JobInfo job = (JobInfo)getInfo(worker);
                    if (job != null)
                        jobs.add(job);
                }

                sender().tell(jobs, self());
            })

            .match(BaseMessage.class, message -> message.getType().equals("online-jobs"), message -> {

                // build a list of current jobs
                JobInfos jobs = new JobInfos();
                for (ActorRef worker : this.onlineJobs.values()) {

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

                JobsSummary summary = new JobsSummary(this.offlineJobs.size(),
                        this.onlineJobs.size(),
                        this.streamingSources.size());
                sender().tell(summary, self());
            })

            .match(Terminated.class, t -> {

                // clean up terminated actors

                String actorName = t.actor().path().name();

                if (isUUID(actorName)) {
                    // if the job name is a uuid - clean up completed jobs

                    UUID jobId = UUID.fromString(actorName);
                    AnalyticsJob job = analyticJobs.get(jobId);
                    if (job.getMode() == Mode.OFFLINE && this.offlineJobs.containsKey(job.getKey().getId())) {
                        this.offlineJobs.remove(job.getKey().getId());
                    }
                    else if (job.getMode() == Mode.ONLINE && this.onlineJobs.containsKey(job.getKey().getId())) {
                        this.onlineJobs.remove(job.getKey().getId());

                        // tell the kafka consumer that a spark job has been terminated
                        kafkaConsumer.tell(new ConsumeJob(job.getKey().getId(), "", ConsumeJob.ConsumeState.END), self());
                    }

                    analyticJobs.remove(jobId);
                } else if (this.streamingSources.containsKey(actorName)) {

                    // clean up the streaming source data actor
                    this.streamingSources.remove(actorName);
                }

                if (this.client != null) {
                    JobsSummary summary = new JobsSummary(this.offlineJobs.size(),
                            this.onlineJobs.size(),
                            this.streamingSources.size());
                    this.client.tell(summary, self());
                }

            }).build()
        );

        kafkaConsumer = context().actorOf(KafkaConsumer.props());
    }

    private JobInfo createFailedJobInfo(JobRequest request, String message){

        JobInfo jobInfo = new JobInfo(UUID.randomUUID(),
                request.getDiagramId(),
                request.getDiagramName(),
                request.getMode());
        jobInfo.setState(JobStates.FAILED);
        jobInfo.setMessage(message);

        return jobInfo;
    }

    private Object getInfo(ActorRef actor) {

        try {
            Timeout timeout = new Timeout(Duration.create(20, TimeUnit.SECONDS));
            Future<Object> future = Patterns.ask(actor, "info", timeout);
            return Await.result(future, timeout.duration());
        }
        catch(Exception ex){
            return null;
        }
    }

    /**
     * apply a regex to determine whether a string is a uuid
     * @param data
     * @return
     */
    private boolean isUUID(String data){
        return (data.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"));
    }
}