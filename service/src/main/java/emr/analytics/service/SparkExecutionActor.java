package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.service.jobs.SparkJob;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;
import emr.analytics.service.spark.RuntimeMessenger;
import emr.analytics.service.spark.SparkCompiler;
import org.apache.spark.SparkContext;

import java.util.UUID;

public class SparkExecutionActor extends AbstractActor {

    private ActorRef jobStatusActor;
    private SparkContext sparkContext;

    public static Props props(ActorRef jobStatusActor, SparkContext sparkContext){
        return Props.create(SparkExecutionActor.class, jobStatusActor, sparkContext);
    }

    public SparkExecutionActor(ActorRef jobStatusActor, SparkContext sparkContext){

        this.jobStatusActor = jobStatusActor;
        this.sparkContext = sparkContext;

        receive(ReceiveBuilder
            .match(SparkJob.class, job -> {

                // report that the job has been started
                this.jobStatusActor.tell(new JobStatus(job.getId(), JobStatusTypes.STARTED), self());

                // create a spark streaming compiler and run
                SparkCompiler compiler = new SparkCompiler(this.sparkContext,
                    job.getSource(),
                    new SparkMessenger(job.getId(), this.jobStatusActor));
                compiler.run();

                // report completion
                this.jobStatusActor.tell(new JobStatus(job.getId(), JobStatusTypes.COMPLETED), self());
            })
            .build());
    }

    public class SparkMessenger implements RuntimeMessenger {
        private UUID jobId;
        private ActorRef actor;

        public SparkMessenger(UUID jobId, ActorRef actor){
            this.jobId = jobId;
            this.actor = actor;
        }

        public void send(String key, String value) {

            // report progress
            this.actor.tell(new JobProgress(this.jobId, key, value), null);
        }
    }
}

