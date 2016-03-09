package emr.analytics.service;

import akka.actor.*;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.messages.*;
import emr.analytics.service.interpreters.*;

import java.util.UUID;

public abstract class ExecutionActor extends AbstractActor implements InterpreterNotificationHandler {

    protected UUID diagramId;
    protected String diagramName;
    protected Mode mode;
    protected TargetEnvironments targetEnvironment;
    protected TaskRequest currentRequest;

    protected ActorRef statusManager;
    protected Interpreter interpreter;

    public ExecutionActor(UUID diagramId, String diagramName, TargetEnvironments targetEnvironment, Mode mode){

        this.diagramId = diagramId;
        this.diagramName = diagramName;
        this.mode = mode;
        this.targetEnvironment = targetEnvironment;

        this.init();
    }

    /**
     * handle interpreter notifications
     * @param notification
     */
    public void notify(InterpreterNotification notification){

        TaskStatus status;
        switch(notification.getKey()){
            case "DATA":
                status = new TaskData(currentRequest.getId(),
                        currentRequest.getSessionId(),
                        diagramId,
                        diagramName,
                        mode,
                        notification.getValue());
                break;
            case "OUT":
                status = new TaskOutput(currentRequest.getId(),
                        currentRequest.getSessionId(),
                        diagramId,
                        diagramName,
                        mode,
                        notification.getValue());
                break;
            case "ERROR":
                status = new TaskError(currentRequest.getId(),
                        currentRequest.getSessionId(),
                        diagramId,
                        diagramName,
                        mode,
                        notification.getValue());
                break;
            default:
                status = new TaskNotification(currentRequest.getId(),
                        currentRequest.getSessionId(),
                        diagramId,
                        diagramName,
                        mode,
                        notification.getKey(),
                        notification.getValue());
        }

        statusManager.tell(status, self());
    }

    /**
     * Wrap the describe into an AnalyticsDescribe object and associate with the current diagram id
     * @param describe Dataframe statistics summary
     */
    public void describe(Describe describe){
        statusManager.tell(new AnalyticsDescribe(currentRequest.getId(),
                currentRequest.getSessionId(),
                diagramId,
                describe), self());
    }

    /**
     * Wrap the features into an AnalyticsData object and associate with the current diagram id
     * @param features
     */
    public void collect(Features features) {
        statusManager.tell(new AnalyticsData(currentRequest.getId(),
                currentRequest.getSessionId(),
                diagramId,
                features), self());
    }

    /**
     * initialize the execution actor
     */
    protected void init(){
        // initialize interpreter
        this.interpreter = InterpreterFactory.get(this.diagramName,
                this,
                this.targetEnvironment,
                this.mode,
                TaskProperties.getInstance().getProperties());
        this.interpreter.start();
    }

    /**
     * stop the execution actor
     */
    protected void stop(){
        this.interpreter.stop();
    }

    /**
     * create a new status manager
     * @param client: the client requesting the task
     */
    protected void createStatusManager(ActorRef client){
        // create the actor that manages the task status
        this.statusManager = context().actorOf(TaskStatusManager.props(self(),
                client,
                diagramId,
                diagramName,
                this.mode),
                "TaskStatusManager");
    }

    protected void runTask(TaskRequest request, String source){
        // capture the task id
        currentRequest = request;

        // notify the status manager that a task is starting
        statusManager.tell(new TaskStarted(currentRequest.getId(),
                currentRequest.getSessionId(),
                this.diagramId,
                this.diagramName,
                this.mode,
                source), self());

        // run the task's source code
        this.run(source);
    }

    protected void run(String source){

        new Thread(() -> {
            InterpreterResult result = interpreter.interpret(source);

            TaskStatus status;
            switch(result.getState()){
                case SUCCESS:
                    status = new TaskCompleted(currentRequest.getId(),
                            currentRequest.getSessionId(),
                            diagramId,
                            diagramName,
                            mode);
                    statusManager.tell(status, self());
                    break;
                case FAILURE:
                    status = new TaskFailed(currentRequest.getId(),
                            currentRequest.getSessionId(),
                            diagramId,
                            diagramName,
                            mode,
                            result.getMessage());
                    statusManager.tell(status, self());
                    break;
            }

        }).start();
    }

    protected void terminateTask(){

        statusManager.tell(new TaskTerminated(currentRequest.getId(),
                currentRequest.getSessionId(),
                this.diagramId,
                this.diagramName,
                this.mode), self());
    }
}
