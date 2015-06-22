package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import emr.analytics.service.jobs.ProcessJob;
import emr.analytics.service.messages.JobCompleted;
import emr.analytics.service.messages.JobFailed;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.messages.JobStarted;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessExecutionActor extends AbstractActor {

    private ActorRef _jobStatusActor;

    public static Props props(ActorRef jobStatusActor){
        return Props.create(ProcessExecutionActor.class, jobStatusActor);
    }

    public ProcessExecutionActor(ActorRef jobStatusActor){

        _jobStatusActor = jobStatusActor;

        receive(ReceiveBuilder
            .match(ProcessJob.class, job -> {

                _jobStatusActor.tell(new JobStarted(job.getId(), job.getMode()), self());

                ProcessBuilder builder = job.getProcess();

                System.out.println(builder.command().toString());

                // execute process
                try {
                    Process process = builder.start();

                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    String lineRead;
                    while ((lineRead = in.readLine()) != null) {
                        _jobStatusActor.tell(new JobProgress(job.getId(), job.getMode(), lineRead), self());
                    }

                    int complete = process.waitFor();

                    if (complete != 0) {
                        // job failed

                        JobFailed message = new JobFailed(job.getId(), job.getMode());
                        _jobStatusActor.tell(message, self());

                        while ((lineRead = err.readLine()) != null) {

                            // todo: send status update
                            System.err.println(lineRead);
                        }
                    } else {

                        JobCompleted message = new JobCompleted(job.getId(), job.getMode());
                        _jobStatusActor.tell(message, self());
                    }
                } catch (IOException | InterruptedException ex) {

                    // todo: handle
                    System.out.println(ex.toString());
                }
            })
            .build());
        }
    }
