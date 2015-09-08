package emr.analytics.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.testkit.JavaTestKit;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.models.messages.JobRequest;
import emr.analytics.models.messages.JobStates;
import emr.analytics.service.jobs.PythonJob;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class ProcessActorTest {

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
    public void testProcess() throws Exception {

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
            PythonJob job = new PythonJob(request);

            // create a test probe
            final JavaTestKit probe = new JavaTestKit(system);

            // create process actor
            final ActorRef processActor = system.actorOf(Props.create(ProcessActor.class, probe.getRef(), job));

            // add processActor to the death watch
            watch(processActor);

            processActor.tell("info", getRef());
            info = expectMsgClass(JobInfo.class);
            Assert.assertEquals("Diagram id is not correct.", diagramId, info.getDiagramId());
            Assert.assertEquals("Diagram mode is not correct.", mode, info.getMode());
            Assert.assertEquals("Job Info was expected to be in the created state.", JobStates.CREATED, info.getState());

            // send job
            processActor.tell("start", getRef());

            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());

            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be BLOCK,0.", "BLOCK,0", info.lastVariableValue("STATE"));
            Assert.assertArrayEquals("The list should be equal to [ 'BLOCK,0' ].", new String[]{"BLOCK,0"}, info.listVariableValues("STATE").toArray());

            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be BLOCK,1.", "BLOCK,1", info.lastVariableValue("STATE"));
            Assert.assertArrayEquals("The list should be equal to [ 'BLOCK,0', 'BLOCK,1' ].", new String[]{"BLOCK,0", "BLOCK,1"}, info.listVariableValues("STATE").toArray());

            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be BLOCK,2.", "BLOCK,2", info.lastVariableValue("STATE"));
            Assert.assertArrayEquals("The list should be equal to [ 'BLOCK,0', 'BLOCK,1', 'BLOCK,2' ].", new String[]{"BLOCK,0", "BLOCK,1", "BLOCK,2"}, info.listVariableValues("STATE").toArray());

            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the completed state.", JobStates.COMPLETED, info.getState());

            // confirm process actor terminates itself
            final Terminated msg = expectMsgClass(Terminated.class);
            Assert.assertEquals(msg.getActor(), processActor);
        }};
    }
}