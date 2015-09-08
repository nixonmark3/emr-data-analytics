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
import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.messages.JobFailed;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class JobStatusActorTest {

    public static class MockJob extends AnalyticsJob {

        public MockJob(UUID id, Mode mode, String diagramName, String source){

            this(new JobRequest(id,
                    mode,
                    TargetEnvironments.PYTHON,
                    diagramName,
                    source, ""));
        }

        public MockJob(JobRequest request){
            super(request);
        }
    }

    static ActorSystem system;
    static final UUID diagramId = UUID.randomUUID();
    static final String diagramName = "test diagram";
    static final String source = "x = 1";
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
    public void testJobStatus() throws Exception {

        /**
         * wrap the test method into a testkit constructor
         */
        new JavaTestKit(system) {{

            JobStatus status;
            JobInfo info;

            // create a mock job to pass to initialize the JobStatus actor
            MockJob job = new MockJob(diagramId,
                    mode,
                    diagramName,
                    source);

            // create a test probe
            final JavaTestKit probe = new JavaTestKit(system);

            // create the job status actor by passing the probe and the mock Job
            final ActorRef jobStatus = system.actorOf(Props.create(JobStatusActor.class, getRef(), probe.getRef(), job));

            // retrieve the initialized job info
            jobStatus.tell("info", getRef());

            // confirm a jobInfo class object is returned and it is in the proper state
            info = expectMsgClass(JobInfo.class);
            Assert.assertEquals("Diagram id is not correct.", diagramId, info.getDiagramId());
            Assert.assertEquals("Diagram mode is not correct.", mode, info.getMode());
            Assert.assertEquals("Job Info was expected to be in the created state.", JobStates.CREATED, info.getState());

            // start the job and verify resulting job info
            status = new JobStatus(JobStatusTypes.STARTED);
            jobStatus.tell(status, getRef());
            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertNotNull("Created date should exist.", info.getStarted());

            // send progress and verify
            status = new JobProgress("VAL", "1");
            jobStatus.tell(status, getRef());
            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be 1.", "1", info.lastVariableValue("VAL"));
            Assert.assertArrayEquals("The list should be equal to [1].", new String[]{"1"}, info.listVariableValues("VAL").toArray());

            status = new JobProgress("VAL", "2");
            jobStatus.tell(status, getRef());
            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the running state.", JobStates.RUNNING, info.getState());
            Assert.assertEquals("The last progress variable should be 2.", "2", info.lastVariableValue("VAL"));
            Assert.assertArrayEquals("The list should be equal to [1, 2].", new String[]{"1", "2"}, info.listVariableValues("VAL").toArray());

            // send complete
            status = new JobStatus(JobStatusTypes.COMPLETED);
            jobStatus.tell(status, getRef());
            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the completed state.", JobStates.COMPLETED, info.getState());
            Assert.assertNotNull("Completed date should exist.", info.getCompleted());

            // confirm a finalize message has been sent
            expectMsgEquals("finalize");

            // send failure
            String message = "Job Failed!";
            status = new JobFailed(message);
            jobStatus.tell(status, getRef());
            info = probe.expectMsgClass(JobInfo.class);
            Assert.assertEquals("Job Info was expected to be in the failed state.", JobStates.FAILED, info.getState());
            Assert.assertEquals("Job Info message was not the expected value.", message, info.getMessage());

            // confirm a finalize message has been sent
            expectMsgEquals("finalize");

            jobStatus.tell(PoisonPill.getInstance(), getRef());
        }};
    }
}