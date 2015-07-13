package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.models.messages.JobStates;
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.jobs.AnalyticsJobException;
import emr.analytics.service.messages.*;

import java.util.Date;

/**
 * Tracks a jobs current state, forwarding each update to the configured client
 */
public class JobStatusActor extends AbstractActor {

    private final ActorRef parent;
    private final ActorRef client;
    private final JobInfo jobInfo;

    public static Props props(ActorRef parent, ActorRef client, AnalyticsJob job) {
        return Props.create(JobStatusActor.class, parent, client, job);
    }

    public JobStatusActor(ActorRef parent, ActorRef client, AnalyticsJob job){

        this.parent = parent;
        this.client = client;
        this.jobInfo = new JobInfo(job.getId(), job.getKey().getId(), job.getDiagramName(), job.getKey().getMode());

        receive(ReceiveBuilder

            .match(JobFailed.class, status -> {

                updateStatus(status);
                this.jobInfo.setMessage(status.getMessage());
                this.client.tell(new JobInfo(this.jobInfo), self());

                SendFinalize();
            })

            .match(JobProgress.class, status -> {

                updateStatus(status);
                this.jobInfo.addVariable(status.getProgressKey(), status.getProgressValue());

                this.client.tell(new JobInfo(this.jobInfo), self());
            })

            .match(JobStatus.class, status -> {

                updateStatus(status);
                this.client.tell(new JobInfo(this.jobInfo), self());

                if (this.jobInfo.isDone())
                    SendFinalize();
            })

            .match(String.class, s -> s.equals("info"), s -> {

                sender().tell(new JobInfo(this.jobInfo), self());

            }).build()
        );
    }

    /**
     * notify parent that the job has been completed
     */
    private void SendFinalize(){
        parent.tell("finalize", self());
    }

    /**
     * Validate the job status type relative to the current job info's state and set job info properties
     * @param status
     * @throws AnalyticsJobException
     */
    private void updateStatus(JobStatus status) throws AnalyticsJobException {

        switch(status.getStatusType()){

            case STARTED:

                System.out.println("Received job started message.");

                // verify the job info object is in the created state
                if (jobInfo.getState() != JobStates.CREATED)
                    throw new AnalyticsJobException(String.format("The job cannot be started because it is in an invalid state: %s.",
                            jobInfo.getState().toString()));

                jobInfo.setStarted(new Date());
                jobInfo.setState(JobStates.RUNNING);

                break;

            case PROGRESS:

                // verify the job info object is in the running state
                if (jobInfo.getState() != JobStates.RUNNING)
                    throw new AnalyticsJobException(String.format("Job progress cannot be captured because it is in an invalid state: %s.",
                            jobInfo.getState().toString()));

                break;

            case COMPLETED:

                // verify the job info object is in the running state
                if (jobInfo.getState() != JobStates.RUNNING)
                    throw new AnalyticsJobException(String.format("The job cannot be completed because it is in an invalid state: %s.",
                            jobInfo.getState().toString()));

                jobInfo.setCompleted(new Date());
                jobInfo.setState(JobStates.COMPLETED);

                break;

            case STOPPED:

                // if the job is already done - exit
                if (jobInfo.isDone())
                    return;

                jobInfo.setCompleted(new Date());
                jobInfo.setState(JobStates.STOPPED);

                break;

            case FAILED:

                jobInfo.setCompleted(new Date());
                jobInfo.setState(JobStates.FAILED);

                break;

            default:
                throw new AnalyticsJobException(String.format("The specified job status, %s, is not supported.",
                        status.getStatusType().toString()));
        }
    }
}
