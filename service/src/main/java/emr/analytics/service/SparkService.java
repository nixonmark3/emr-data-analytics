package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.database.DefinitionsRepository;
import emr.analytics.database.ResultsRepository;
import emr.analytics.diagram.interpreter.PySparkCompiler;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.AnalyticsTask;
import emr.analytics.models.messages.DiagramTaskRequest;
import emr.analytics.models.messages.TaskRequest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Creates a new actor system in a separate process, associates with it remotely, and passes the spark job for execution
 */
public class SparkService extends AbstractActor {

    private ActorRef client = null;
    private ActorRef spark = null;

    private UUID diagramId;
    private String diagramName;
    private TargetEnvironments targetEnvironment;
    private Mode mode;

    private PySparkCompiler compiler;

    private Queue<TaskRequest> requests;

    public static Props props(ActorRef client, UUID id, UUID diagramId, String diagramName, TargetEnvironments targetEnvironment, Mode mode, String host, String port){

        return Props.create(SparkService.class,
                client,
                id,
                diagramId,
                diagramName,
                targetEnvironment,
                mode,
                host,
                port);
    }

    public SparkService(ActorRef client, UUID id, UUID diagramId, String diagramName, TargetEnvironments targetEnvironment, Mode mode, String host, String port) throws SparkServiceException {

        this.client = client;
        this.diagramId = diagramId;
        this.diagramName = diagramName;
        this.targetEnvironment = targetEnvironment;
        this.mode = mode;

        this.requests = new LinkedList<TaskRequest>();

        // instantiate the compiler
        this.compiler = createCompiler();

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

                    while(this.requests.size() > 0)
                        spark.tell(this.requests.remove(), self());
                })

                /**
                 * Request received to evaluate a diagram
                 */
                .match(DiagramTaskRequest.class, request -> {

                    // retrieve the models
                    MongoService mongoService = MongoService.getInstance();
                    ResultsRepository repository = new ResultsRepository(mongoService.getConnection());
                    compiler.setModels(repository.getPersistedOutputs(request.getDiagram()));

                    // compile
                    String source = compiler.compile(request.getDiagram());
                    request.setSource(source);

                    // send
                    this.sendTask(request);
                })

                /**
                 * Receive new spark jobs - depending on state - either send or queue
                 */
                .match(TaskRequest.class, this::sendTask)

                /**
                 * initiate the termination of this task
                 */
                .match(String.class, s -> s.equals("stop"), s -> {

                    // notify the job status actor that the job is being stopped
                    spark.tell(s, self());
                })

                /**
                 * synchronously forward a request to the spark process for a summary of the running task
                 */
                .match(String.class, s -> s.equals("task"), s -> {

                    Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
                    Future<Object> future = Patterns.ask(spark, s, timeout);
                    AnalyticsTask task = (AnalyticsTask) Await.result(future, timeout.duration());

                    sender().tell(task, self());
                })

                /**
                 * the spark process was been terminated, administor a poison pill to self
                 */
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

    /**
     * Create an instance of pyspark compiler
     * @return PySpark Compiler
     */
    private PySparkCompiler createCompiler(){

        // retrieve the set of definitions
        MongoService mongoService = MongoService.getInstance();
        DefinitionsRepository definitionsRepository = new DefinitionsRepository(mongoService.getConnection());
        Map<String, Definition> definitions = definitionsRepository.toMap();

        return new PySparkCompiler(this.diagramId, definitions, TaskProperties.getInstance().getProperties());
    }

    /**
     * Based on the state of the SparkProcess, send or queue the specified task request
     * @param request: a task request
     */
    private void sendTask(TaskRequest request){
        if (spark != null)
            spark.tell(request, self());
        else
            this.requests.add(request);
    }

    /**
     * Create a new JVM process and instantiate SparkProcess class
     * @param id
     * @param system
     * @param host
     * @param port
     * @throws SparkServiceException
     */
    private void runProcess(String id, String system, String host, String port) throws SparkServiceException {

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

            // if it exists - add the analytics configuration file path
            String propertiesFilePath = TaskProperties.getInstance().getProperties().getProperty("analytics.configuration");
            if (propertiesFilePath != null)
                arguments.add("-Danalytics.configuration=" + propertiesFilePath);

            // add class path and arguments
            arguments.add("-classpath");
            arguments.add(pathBuilder.substring(0, pathBuilder.length() - 1));
            arguments.add(SparkProcess.class.getCanonicalName());
            arguments.add(id);
            arguments.add(system);
            arguments.add(host);
            arguments.add(port);
            arguments.add(this.diagramId.toString());
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
            throw new SparkServiceException(String.format("Message: %s", ex.toString()));
        }
    }
}
