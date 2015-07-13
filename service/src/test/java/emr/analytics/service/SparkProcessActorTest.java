package emr.analytics.service;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.testkit.JavaTestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.models.messages.JobRequest;
import emr.analytics.models.messages.JobStates;
import emr.analytics.service.jobs.SparkJob;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class SparkProcessActorTest {

    static ActorSystem system;
    static Config config;

    @BeforeClass
    public static void setup() {

        config = ConfigFactory.load("service");
        system = ActorSystem.create("Test-System", config);
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testSparkProcess() throws Exception {

        /**
         * wrap the test method into a testkit constructor
         */
        /*new JavaTestKit(system) {{

            JobInfo info;

            String host = config.getString("akka.remote.netty.tcp.hostname");
            String port = config.getString("akka.remote.netty.tcp.port");

            // build source code string
            StringBuilder source = new StringBuilder();
            source.append("val data = Array(1, 2, 3, 4)\n");
            source.append("val distData = sc.parallelize(data)\n");
            source.append("val result = distData.map(x => x * 2).sum().toInt\n");
            source.append("messenger.send(\"Value\", result.toString)\n");
            source.append("messenger.send(\"Value\", result.toString)\n");

            JobRequest request = new JobRequest(UUID.randomUUID(),
                    Mode.ONLINE,
                    TargetEnvironments.SPARK,
                    "test diagram", source.toString());
            SparkJob job = new SparkJob(request);

            // create a test probe
            final JavaTestKit probe = new JavaTestKit(system);

            // create spark execution actor
            final ActorRef sparkProcessActor = system.actorOf(Props.create(SparkProcessActor.class,
                            probe.getRef(),
                            job,
                            host,
                            port),
                    job.getId().toString());

            watch(sparkProcessActor);

            info = probe.expectMsgClass(duration("10 second"), JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());

            info = probe.expectMsgClass(duration("10 second"), JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be 20.", "20", info.lastVariableValue("Value"));
            Assert.assertArrayEquals("The list should be equal to [ '20' ].", new String[]{"20"}, info.listVariableValues("Value").toArray());

            info = probe.expectMsgClass(duration("10 second"), JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be 20.", "20", info.lastVariableValue("Value"));
            Assert.assertArrayEquals("The list should be equal to [ '20', '20' ].", new String[]{"20", "20"}, info.listVariableValues("Value").toArray());

            info = probe.expectMsgClass(duration("10 second"), JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the completed state.", JobStates.COMPLETED, info.getState());

            // confirm process actor terminates itself
            final Terminated msg = expectMsgClass(duration("10 second"), Terminated.class);
            Assert.assertEquals(msg.getActor(), sparkProcessActor);
        }};*/
    }
}