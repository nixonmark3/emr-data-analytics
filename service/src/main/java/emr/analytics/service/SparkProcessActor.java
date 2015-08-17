package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Creates a new actor system in a separate process, associates with it remotely, and passes the spark job for execution
 */
public class SparkProcessActor extends AbstractActor {

    private ActorRef client = null;
    private ActorRef spark = null;

    private String diagramName;
    private TargetEnvironments targetEnvironment;
    private Mode mode;

    private Queue<SparkJob> jobs;

    public static Props props(ActorRef client, UUID id, String diagramName, TargetEnvironments targetEnvironment, Mode mode, String host, String port){

        return Props.create(SparkProcessActor.class,
                client,
                id,
                diagramName,
                targetEnvironment,
                mode,
                host,
                port);
    }

    public SparkProcessActor(ActorRef client, UUID id, String diagramName, TargetEnvironments targetEnvironment, Mode mode, String host, String port) throws SparkProcessException {

        this.client = client;
        this.diagramName = diagramName;
        this.targetEnvironment = targetEnvironment;
        this.mode = mode;

        this.jobs = new LinkedList<SparkJob>();

        receive(ReceiveBuilder

                /**
                 * After the spark actor starts up, it will make contact by sending a reference to itself
                 */
                .match(ActorRef.class, actor -> {

                    // reference spark actor
                    spark = actor;
                    context().watch(spark);

                    // send reference to client and job
                    spark.tell(this.client, self());

                    // if a job exists - send it
                    if (jobs.size() > 0)
                        spark.tell(this.jobs.remove(), self());
                })

                /**
                 * Receive new spark jobs - depending on state - either send or queue
                 */
                .match(SparkJob.class, job -> {

                    if (spark != null)
                        spark.tell(job, self());
                    else
                        this.jobs.add(job);
                })

                /**
                 * initiate the stopping of this job
                 */
                .match(String.class, s -> s.equals("stop"), s -> {

                    // notify the job status actor that the job is being stopped
                    spark.tell(s, self());
                })

                /**
                 * forward the info request to the spark actor
                 */
                .match(String.class, s -> s.equals("info"), s -> {

                    Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
                    Future<Object> future = Patterns.ask(spark, s, timeout);
                    JobInfo jobInfo = (JobInfo) Await.result(future, timeout.duration());

                    sender().tell(jobInfo, self());
                })

                .match(Terminated.class, terminated -> {

                    // the spark actor has been terminated - terminate self
                    self().tell(PoisonPill.getInstance(), self());
                })

                .matchAny(this::unhandled)

                .build());

        // create the spark process
        this.runProcess(id.toString(),
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
            arguments.add(this.diagramName);
            arguments.add(this.targetEnvironment.toString());
            arguments.add(this.mode.toString());

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
