package emr.analytics.service;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.JobRequest;
import emr.analytics.service.jobs.SparkJob;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class SparkExecutionActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testSparkExecution() throws Exception {

        /**
         * wrap the test method into a testkit constructor
         */
        new JavaTestKit(system) {{

            JobStatus status;

            // build source code string
            StringBuilder source = new StringBuilder();
            source.append("val data = Array(1, 2, 3, 4)\n");
            source.append("val distData = sc.parallelize(data)\n");
            source.append("val result = distData.map(x => x * 2).sum().toInt\n");
            source.append("messenger.send(\"Value\", result.toString)\n");

            JobRequest request = new JobRequest(UUID.randomUUID(),
                    Mode.ONLINE,
                    TargetEnvironments.SPARK,
                    "test diagram", source.toString());
            SparkJob job = new SparkJob(request);

            // create a spark context
            SparkConf conf = new SparkConf()
                    .setMaster("local[2]")
                    .setAppName("SparkExecutionTest");
            SparkContext sparkContext = new SparkContext(conf);

            // create a test probe
            final JavaTestKit probe = new JavaTestKit(system);

            // create spark execution actor
            final ActorRef sparkExecutionActor = system.actorOf(Props.create(SparkExecutionActor.class,
                    probe.getRef(),
                    sparkContext));

            // send job
            sparkExecutionActor.tell(job, getRef());

            status = probe.expectMsgClass(JobStatus.class);
            Assert.assertEquals("This status message should have been to report that the job was starting.", JobStatusTypes.STARTED, status.getStatusType());

            status = probe.expectMsgClass(JobStatus.class);
            Assert.assertEquals("This status message should have been to report that the job was starting.", JobStatusTypes.PROGRESS, status.getStatusType());
            Assert.assertEquals("This progress key is not correct.", "Value", ((JobProgress)status).getProgressKey());
            Assert.assertEquals("This progress value is not correct.", "20", ((JobProgress)status).getProgressValue());

            status = probe.expectMsgClass(JobStatus.class);
            Assert.assertEquals("This status message should have been to report that the job was completed.", JobStatusTypes.COMPLETED, status.getStatusType());

            sparkExecutionActor.tell(PoisonPill.getInstance(), getRef());

            sparkContext.stop();
        }};
    }
}