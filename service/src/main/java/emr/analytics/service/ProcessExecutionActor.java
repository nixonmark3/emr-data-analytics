package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import emr.analytics.service.jobs.ProcessJob;
import emr.analytics.service.messages.JobFailed;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.messages.JobStatus;
import emr.analytics.service.messages.JobStatusTypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Execute the specified process job
 */
public class ProcessExecutionActor extends AbstractActor {

    private ActorRef jobStatusActor;

    public static Props props(ActorRef jobStatusActor){
        return Props.create(ProcessExecutionActor.class, jobStatusActor);
    }

    public ProcessExecutionActor(ActorRef jobStatusActor){

        this.jobStatusActor = jobStatusActor;

        receive(ReceiveBuilder
            .match(ProcessJob.class, job -> {

                ProcessBuilder builder = job.getProcess();

                // report that the job has been started
                this.jobStatusActor.tell(new JobStatus(job.getId(), JobStatusTypes.STARTED), self());

                // execute process
                Process process = null;
                try {
                    process = builder.start();

                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    String lineRead;
                    while ((lineRead = in.readLine()) != null) {

                        // report progress
                        this.jobStatusActor.tell(new JobProgress(job.getId(), "STATE", lineRead), self());
                    }

                    int complete = process.waitFor();

                    if (complete != 0) {
                        // job failed

                        StringBuilder stringBuilder = new StringBuilder();
                        while ((lineRead = err.readLine()) != null) {

                            stringBuilder.append(lineRead);
                            stringBuilder.append("\n");
                        }

                        System.err.print(stringBuilder.toString());

                        // report failure
                        this.jobStatusActor.tell(new JobFailed(job.getId(), stringBuilder.toString()), self());
                    } else {

                        // report completion
                        this.jobStatusActor.tell(new JobStatus(job.getId(), JobStatusTypes.COMPLETED), self());
                    }
                } catch (IOException | InterruptedException ex) {

                    System.err.print(ex.toString());

                    this.jobStatusActor.tell(new JobFailed(job.getId(), ex.toString()), self());
                    if (process != null){
                        process.destroyForcibly();
                    }
                }
            })
            .build());
        }
    }
