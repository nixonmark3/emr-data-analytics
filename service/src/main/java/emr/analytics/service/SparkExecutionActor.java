package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.service.jobs.SparkStreamingJob;
import emr.analytics.service.spark.SparkStreamingCompiler;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.Duration;

public class SparkExecutionActor extends AbstractActor {

    private ActorRef _jobStatusActor;
    private String[] _jars = new String[] {
        "../utilities/spark-algorithms/target/scala-2.10/spark-algorithms_2.10-1.0.jar"
    };

    public static Props props(ActorRef jobStatusActor){
        return Props.create(SparkExecutionActor.class, jobStatusActor);
    }

    public SparkExecutionActor(ActorRef jobStatusActor){

        _jobStatusActor = jobStatusActor;

        receive(ReceiveBuilder
            .match(SparkStreamingJob.class, job -> {

                // todo: provide feedback to job status actor

                // create a spark streaming context
                SparkConf conf = new SparkConf()
                    .setMaster("local[2]")
                    .setAppName(job.getDiagramName().replace(" ", ""))
                    .setJars(_jars);

                StreamingContext ssc = new StreamingContext(conf, new Duration(500));

                // pass context back to sender
                sender().tell(ssc, self());

                // create a spark streaming compiler and run
                SparkStreamingCompiler compiler = new SparkStreamingCompiler(job.getSource());
                compiler.run(ssc);
            })
            .build());
    }
}

