package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.service.jobs.JobMode;
import emr.analytics.service.messages.JobCompleted;
import emr.analytics.service.messages.JobFailed;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.messages.JobStarted;
import emr.analytics.service.processes.AnalyticsProcessBuilder;
import emr.analytics.service.processes.ProcessBuilderException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class JobExecutionActor extends AbstractActor {

    private UUID _jobId;
    private JobMode _mode;
    private ActorRef _jobStatusActor;

    public static Props props(UUID id, JobMode mode, ActorRef jobStatusActor){

        return Props.create(JobExecutionActor.class, id, mode, jobStatusActor);
    }

    public JobExecutionActor(UUID id, JobMode mode, ActorRef jobStatusActor){

        _jobId = id;
        _mode = mode;
        _jobStatusActor = jobStatusActor;

        receive(ReceiveBuilder.
            match(AnalyticsProcessBuilder.class, builder -> {

                _jobStatusActor.tell(new JobStarted(_jobId, _mode), self());

                System.out.println(builder.toString());

                // execute process
                try {
                    Process process = builder.start();

                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    String lineRead;
                    while ((lineRead = in.readLine()) != null) {
                        _jobStatusActor.tell(new JobProgress(_jobId, _mode, lineRead), self());
                    }

                    int complete = process.waitFor();

                    if (complete != 0) {
                        // job failed

                        JobFailed message = new JobFailed(_jobId, _mode);
                        _jobStatusActor.tell(message, self());

                        while ((lineRead = err.readLine()) != null) {

                            // todo: send status update
                            System.err.println(lineRead);
                        }
                    } else {

                        JobCompleted message = new JobCompleted(_jobId, _mode);
                        _jobStatusActor.tell(message, self());
                    }
                } catch (ProcessBuilderException | IOException | InterruptedException ex) {

                    // todo: handle
                    System.out.println(ex.toString());
                }

            }).build()
        );
    }
}
