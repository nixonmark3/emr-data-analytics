package emr.analytics.service;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.models.messages.JobRequest;
import emr.analytics.models.messages.JobStates;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class JobServiceActorTest {

    static ActorSystem system;
    static final UUID diagramId = UUID.randomUUID();
    static final String diagramName = "test diagram";
    static final Mode mode = Mode.OFFLINE;

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
    public void testProcessJob() throws Exception {

        /**
         * wrap the test method into a testkit constructor
         */
        new JavaTestKit(system) {{

            JobInfo info;

            // build source code string
            StringBuilder source = new StringBuilder();
            source.append("import sys\n");
            source.append("x = 0\n");
            source.append("print('{0},{1}'.format('BLOCK', x))\n");
            source.append("sys.stdout.flush()\n");
            source.append("x = x + 1\n");
            source.append("print('{0},{1}'.format('BLOCK', x))\n");
            source.append("sys.stdout.flush()\n");
            source.append("x = x + 1\n");
            source.append("print('{0},{1}'.format('BLOCK', x))\n");
            source.append("sys.stdout.flush()\n");

            // create a python job
            JobRequest request = new JobRequest(diagramId,
                    mode,
                    TargetEnvironments.PYTHON,
                    diagramName,
                    source.toString(), "");

            // create process actor
            final ActorRef jobServiceActor = system.actorOf(Props.create(JobServiceActor.class, "host", "port"));

            jobServiceActor.tell(request, getRef());
            info = expectMsgClass(JobInfo.class);
            Assert.assertEquals("Diagram id is not correct.", diagramId, info.getDiagramId());
            Assert.assertEquals("Diagram mode is not correct.", mode, info.getMode());
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());

            info = expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be BLOCK,0.", "BLOCK,0", info.lastVariableValue("STATE"));
            Assert.assertArrayEquals("The list should be equal to [ 'BLOCK,0' ].", new String[]{"BLOCK,0"}, info.listVariableValues("STATE").toArray());

            info = expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be BLOCK,1.", "BLOCK,1", info.lastVariableValue("STATE"));
            Assert.assertArrayEquals("The list should be equal to [ 'BLOCK,0', 'BLOCK,1' ].", new String[]{"BLOCK,0", "BLOCK,1"}, info.listVariableValues("STATE").toArray());

            info = expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be BLOCK,2.", "BLOCK,2", info.lastVariableValue("STATE"));
            Assert.assertArrayEquals("The list should be equal to [ 'BLOCK,0', 'BLOCK,1', 'BLOCK,2' ].", new String[]{"BLOCK,0", "BLOCK,1", "BLOCK,2"}, info.listVariableValues("STATE").toArray());

            info = expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the completed state.", JobStates.COMPLETED, info.getState());

            jobServiceActor.tell(PoisonPill.getInstance(), getRef());
        }};
    }
}