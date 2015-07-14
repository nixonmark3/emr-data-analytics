package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.messages.*;
import emr.analytics.models.definition.Mode;
import emr.analytics.service.jobs.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class JobServiceActor extends AbstractActor {

    // map jobs by their ids
    private Map<UUID, AnalyticsJob> jobs = new HashMap<UUID, AnalyticsJob>();

    // organize actors by mode {offline, online} and diagram id
    private Map<UUID, ActorRef> offlineJobs = new HashMap<UUID, ActorRef>();
    private Map<UUID, ActorRef> onlineJobs = new HashMap<UUID, ActorRef>();

    private ActorRef client = null;

    public static Props props(String host, String port){ return Props.create(JobServiceActor.class, host, port); }

    public JobServiceActor(String host, String port){

        receive(ReceiveBuilder

            // on remote association the client will pass a reference to itself for reference
            .match(ActorRef.class, actor -> {

                this.client = actor;
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

                    jobs.put(job.getId(), job);
                    offlineJobs.put(job.getKey().getId(), jobActor);
                    context().watch(jobActor);

                    jobActor.tell("start", self());

                } else if (job instanceof SparkJob) {

                    jobActor = context().actorOf(
                            SparkProcessActor.props(sender(), (SparkJob) job, host, port),
                            job.getId().toString());

                    jobs.put(job.getId(), job);
                    onlineJobs.put(job.getKey().getId(), jobActor);
                    context().watch(jobActor);

                } else {

                    // notify the sender that the specified job type is not support
                    sender().tell(createFailedJobInfo(request, "The job specified is not supported."), self());
                    return;
                }

                // proactively send an update jobs summary
                JobsSummary summary = new JobsSummary(this.offlineJobs.size(), this.onlineJobs.size());
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

            .match(BaseMessage.class, message -> message.getType().equals("offline-jobs"), message -> {

                // build a list of current jobs
                JobInfos jobs = new JobInfos();
                for (ActorRef worker : this.offlineJobs.values()) {

                    JobInfo job = getJobInfo(worker);
                    if (job != null)
                        jobs.add(job);
                }

                sender().tell(jobs, self());
            })

            .match(BaseMessage.class, message -> message.getType().equals("online-jobs"), message -> {

                // build a list of current jobs
                JobInfos jobs = new JobInfos();
                for (ActorRef worker : this.onlineJobs.values()) {

                    JobInfo job = getJobInfo(worker);
                    if (job != null)
                        jobs.add(job);
                }

                sender().tell(jobs, self());
            })

            .match(BaseMessage.class, message -> message.getType().equals("jobs-summary"), message -> {

                JobsSummary summary = new JobsSummary(this.offlineJobs.size(), this.onlineJobs.size());
                sender().tell(summary, self());
            })

            .match(Terminated.class, t -> {

                // clean up completed jobs
                UUID jobId = UUID.fromString(t.actor().path().name());

                AnalyticsJob job = jobs.get(jobId);
                if (job.getMode() == Mode.OFFLINE && this.offlineJobs.containsKey(job.getKey().getId()))
                    this.offlineJobs.remove(job.getKey().getId());
                else if (job.getMode() == Mode.ONLINE && this.onlineJobs.containsKey(job.getKey().getId()))
                    this.onlineJobs.remove(job.getKey().getId());

                jobs.remove(jobId);

                if (this.client != null){
                    JobsSummary summary = new JobsSummary(this.offlineJobs.size(), this.onlineJobs.size());
                    this.client.tell(summary, self());
                }

            }).build()
        );

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

    private JobInfo getJobInfo(ActorRef actor) {

        try {
            Timeout timeout = new Timeout(Duration.create(20, TimeUnit.SECONDS));
            Future<Object> future = Patterns.ask(actor, "info", timeout);
            return (JobInfo) Await.result(future, timeout.duration());
        }
        catch(Exception ex){
            return null;
        }
    }
}