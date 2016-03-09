package emr.analytics.service;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import emr.analytics.diagram.interpreter.PythonCompiler;
import emr.analytics.diagram.interpreter.SourceBlock;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.*;
import emr.analytics.service.interpreters.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PythonWorker extends ExecutionActor implements InterpreterNotificationHandler {

    PythonCompiler compiler;

    public static Props props(ActorRef client, UUID diagramId, String diagramName) {
        return Props.create(PythonWorker.class, client, diagramId, diagramName);
    }

    public PythonWorker(ActorRef client, UUID diagramId, String diagramName){
        super(diagramId, diagramName, TargetEnvironments.PYTHON, Mode.OFFLINE);
        this.createStatusManager(client);

        compiler = new PythonCompiler();

        receive(ReceiveBuilder

                /**
                 * Request received to evaluate a single block
                 */
                .match(BlockTaskRequest.class, request -> {

                    // retrieve the source block
                    SourceBlock sourceBlock = compiler.getSourceBlock(request.getBlock());

                    // get the source code
                    String code = sourceBlock.getCode(compiler.getCompiledBlocks());

                    // run the task
                    this.runTask(request, code);
                })

                /**
                 * Request received to evaluate a diagram
                 */
                .match(DiagramTaskRequest.class, request -> {

                    String source = compiler.compile(request.getDiagram());
                    this.runTask(request, source);
                })

                /**
                 * Task request received
                 */
                .match(TaskRequest.class, request -> {

                    this.runTask(request, request.getSource());
                })

                /**
                 * forward the info request to the status actor
                 */
                .match(String.class, s -> s.equals("task"), s -> {

                    Timeout timeout = new Timeout(Duration.create(20, TimeUnit.SECONDS));
                    Future<Object> future = Patterns.ask(statusManager, s, timeout);
                    AnalyticsTask task = (AnalyticsTask) Await.result(future, timeout.duration());

                    sender().tell(task, self());
                })

                /**
                 * stop interpreter and pass poison pill to self
                 */
                .match(String.class, s -> s.equals("finalize"), s -> {
                    interpreter.stop();
                    self().tell(PoisonPill.getInstance(), self());
                })

                /**
                 * request to stop this worker, pass a Task Terinated message to the status manager
                 */
                .match(String.class, s -> s.equals("stop"), s -> {
                    this.terminateTask();
                })

                .build());

    }
}
