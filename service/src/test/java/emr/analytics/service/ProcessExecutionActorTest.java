package emr.analytics.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.JobRequest;
import emr.analytics.service.jobs.PythonJob;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class ProcessExecutionActorTest {

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
    public void testProcessExecution() throws Exception {

        /**
         * wrap the test method into a testkit constructor
         */
        new JavaTestKit(system) {{

            JobStatus status;

            // build source code string
            StringBuilder source = new StringBuilder();
            source.append("import sys\n");
            source.append("x = 0\n");
            source.append("print('{0},{1}'.format('BLOCK', x))\n");
            source.append("sys.stdout.flush()\n");
            source.append("x = x + 1\n");
            source.append("print('{0},{1}'.format('BLOCK', x))\n");
            source.append("sys.stdout.flush()\n");

            // create a python job
            JobRequest request = new JobRequest(UUID.randomUUID(),
                Mode.OFFLINE,
                TargetEnvironments.PYTHON,
                "test diagram", source.toString(), "");
            PythonJob job = new PythonJob(request);

            // create a test probe
            final JavaTestKit probe = new JavaTestKit(system);

            // create process execution actor
            final ActorRef processExecutionActor = system.actorOf(Props.create(ProcessExecutionActor.class, probe.getRef()));

            // send job
            processExecutionActor.tell(job, getRef());

            status = probe.expectMsgClass(JobStatus.class);
            Assert.assertEquals("This status message should have been to report that the job was starting.", JobStatusTypes.STARTED, status.getStatusType());

            status = probe.expectMsgClass(JobStatus.class);
            Assert.assertEquals("This status message should have been to report that the job was starting.", JobStatusTypes.PROGRESS, status.getStatusType());
            Assert.assertEquals("This progress key is not correct.", "STATE", ((JobProgress)status).getProgressKey());
            Assert.assertEquals("This progress value is not correct.", "BLOCK,0", ((JobProgress)status).getProgressValue());

            status = probe.expectMsgClass(JobProgress.class);
            Assert.assertEquals("This status message should have been to report that the job was starting.", JobStatusTypes.PROGRESS, status.getStatusType());
            Assert.assertEquals("This progress key is not correct.", "STATE", ((JobProgress)status).getProgressKey());
            Assert.assertEquals("This progress value is not correct.", "BLOCK,1", ((JobProgress)status).getProgressValue());

            status = probe.expectMsgClass(JobStatus.class);
            Assert.assertEquals("This status message should have been to report that the job was completed.", JobStatusTypes.COMPLETED, status.getStatusType());

            job.removeSource();

            processExecutionActor.tell(PoisonPill.getInstance(), getRef());
        }};
    }
}