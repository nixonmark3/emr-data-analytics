package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.models.interfaces.RuntimeMessenger;
import emr.analytics.service.jobs.SparkJob;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;
import emr.analytics.service.spark.SparkCompiler;
import org.apache.spark.streaming.StreamingContext;

import java.io.Serializable;
import java.util.UUID;

public class SparkExecutionActor extends AbstractActor {

    private ActorRef jobStatusActor;
    private StreamingContext streamingContext;

    public static Props props(ActorRef jobStatusActor, StreamingContext streamingContext){
        return Props.create(SparkExecutionActor.class, jobStatusActor, streamingContext);
    }

    public SparkExecutionActor(ActorRef jobStatusActor, StreamingContext streamingContext){

        this.jobStatusActor = jobStatusActor;
        this.streamingContext = streamingContext;

        receive(ReceiveBuilder
            .match(SparkJob.class, job -> {

                // report that the job has been started
                this.jobStatusActor.tell(new JobStatus(JobStatusTypes.STARTED), self());

                // create a spark streaming compiler and run
                SparkCompiler compiler = new SparkCompiler(this.streamingContext,
                    job.getSource(),
                    new SparkMessenger(job.getId(), this.jobStatusActor));
                compiler.run();

                // report completion
                this.jobStatusActor.tell(new JobStatus(JobStatusTypes.COMPLETED), self());
            })
            .build());
    }

    public class SparkMessenger implements RuntimeMessenger, Serializable {
        private UUID jobId;
        private ActorRef actor;

        public SparkMessenger(UUID jobId, ActorRef actor){
            this.jobId = jobId;
            this.actor = actor;
        }

        public void send(String key, String value) {

            // report progress
            this.actor.tell(new JobProgress(key, value), null);
        }
    }
}

