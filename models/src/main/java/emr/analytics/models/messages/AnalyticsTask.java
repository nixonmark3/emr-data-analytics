package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.structures.CircularQueue;

import java.io.Serializable;
import java.util.UUID;

public class AnalyticsTask implements Serializable {

    private UUID diagramId;
    private String diagramName;
    private Mode mode;
    private TaskStates state;
    private String source;

    private CircularQueue<TaskStatus> statuses;

    public AnalyticsTask(UUID diagramId, String diagramName, Mode mode, int statusCapacity){
        this.diagramId = diagramId;
        this.diagramName = diagramName;
        this.mode = mode;

        this.state = TaskStates.IDLE;
        statuses = new CircularQueue<>(TaskStatus.class, statusCapacity);
    }

    //
    // public methods
    //

    /**
     *
     * @param status
     */
    public void addStatus(TaskStatus status){

        switch(status.getStatusType()){
            case STARTED:

                // verify the task can be started
                if (this.state != TaskStates.IDLE){
                    throw new AnalyticsException(String.format(
                            "The task for diagram, %s, cannot be started because it is not in a valid state : %s.",
                            this.diagramName,
                            this.state.toString()));
                }

                this.source = status.getMessage();
                this.state = TaskStates.RUNNING;
                break;
            case COMPLETED:
            case FAILED:

                // verify the task can be started
                if (this.state != TaskStates.RUNNING){
                    throw new AnalyticsException(String.format(
                            "The task for diagram, %s, cannot be completed because it is not in a valid state : %s.",
                            this.diagramName,
                            this.state.toString()));
                }

                this.source = "";
                this.state = TaskStates.IDLE;
                break;
        }

        this.statuses.add(status);
    }

    /**
     * create a copy of this AnalyticsTask object
     * @return a new AnalyticsTask
     */
    public AnalyticsTask copy(){
        AnalyticsTask copy = new AnalyticsTask(this.diagramId, this.diagramName, this.mode, 0);
        copy.state = this.state;
        copy.statuses = this.statuses;
        return copy;
    }

    public UUID getDiagramId() { return this.diagramId; }

    public String getDiagramName() { return this.diagramName; }

    public Mode getMode() { return this.mode; }

    public String getSource() { return this.source; }

    public enum TaskStates {
        IDLE,
        RUNNING
    }
}
