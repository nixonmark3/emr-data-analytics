package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.service.jobs.SparkJob;
import emr.analytics.service.messages.JobProgress;
import emr.analytics.service.spark.SparkProcess;
import emr.analytics.service.spark.SparkProcessException;
import scala.PartialFunction;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Creates a new actor system in a separate process, associates with it remotely, and passes the spark job for execution
 */
public class SparkProcessActor extends AbstractActor {

    private ActorRef client;
    private ActorRef spark;
    private SparkJob job;
    private PartialFunction<Object, BoxedUnit> active;

    public static Props props(ActorRef client, SparkJob job, String host, String port){
        return Props.create(SparkProcessActor.class, client, job, host, port);
    }

    public SparkProcessActor(ActorRef client, SparkJob job, String host, String port) throws SparkProcessException {

        this.client = client;
        this.job = job;

        receive(ReceiveBuilder

                /**
                 * After the spark actor identifies this actor, it will make context by sending a reference
                 */
                .match(ActorRef.class, actor -> {

                    System.out.println("deploying spark job");

                    spark = actor;

                    context().watch(spark);

                    // send reference to client and job
                    spark.tell(this.client, self());
                    spark.tell(this.job, self());
                })

                /**
                 * prematurely initiate the stopping of this job
                 */
                .match(String.class, s -> s.equals("stop"), s -> {

                    // notify the job status actor that the job is being stopped
                    spark.tell(s, self());
                })

                /**
                 * forward the info request to the status actor
                 */
                .match(String.class, s -> s.equals("info"), s -> {

                    Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
                    Future<Object> future = Patterns.ask(spark, s, timeout);
                    JobInfo jobInfo = (JobInfo) Await.result(future, timeout.duration());

                    sender().tell(jobInfo, self());
                })

                .match(Terminated.class, terminated -> {

                    System.out.println("Terminating spark process actor.");

                    // the spark actor has been terminated - terminate self
                    self().tell(PoisonPill.getInstance(), self());
                })

                .match(ReceiveTimeout.class, timeout -> {
                    // ignore
                })

                .matchAny(this::unhandled)

                .build());

        System.out.println(self().path().name());

        // create the spark process
        this.runProcess(this.job.getId().toString(),
                context().system().name(),
                host,
                port);
    }

    private void runProcess(String id, String system, String host, String port) throws SparkProcessException {

        try {

            String operatingSystemName = System.getProperty("os.name").toLowerCase();

            StringBuilder pathBuilder = new StringBuilder();
            URL[] urls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
            for (URL url : urls) {

                pathBuilder.append(url.toURI().toString().replace("file:", ""));

                if (operatingSystemName.startsWith("windows")) {

                    pathBuilder.append(";");
                }
                else {

                    pathBuilder.append(":");
                }
            }

            List<String> arguments = new ArrayList<String>();
            arguments.add("java");
            arguments.add("-classpath");
            arguments.add(pathBuilder.substring(0, pathBuilder.length() - 1));
            arguments.add(SparkProcess.class.getCanonicalName());
            arguments.add(id);
            arguments.add(system);
            arguments.add(host);
            arguments.add(port);

            ProcessBuilder builder = new ProcessBuilder(arguments);
            Process process = builder.start();

            new Thread(() -> {

                try {

                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    String lineRead;
                    while ((lineRead = in.readLine()) != null) {

                        System.out.println(lineRead);
                    }

                    int complete = process.waitFor();

                    if (complete != 0) {

                        StringBuilder stringBuilder = new StringBuilder();
                        while ((lineRead = err.readLine()) != null) {

                            stringBuilder.append(lineRead);
                            stringBuilder.append(" ");
                        }

                        System.err.print(stringBuilder.toString());
                    }
                }
                catch(Exception ex) {

                    System.err.println(ex.toString());
                }

            }).start();
        }
        catch(Exception ex) {

            System.err.println(ex.getMessage());
            throw new SparkProcessException(String.format("Message: %s", ex.toString()));
        }
    }
}
