package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;

import java.io.Serializable;
import java.util.*;

public class JobInfo implements Serializable {

    private UUID jobId;
    private UUID diagramId;
    private String diagramName;
    private Mode mode;
    private JobStates state;
    private Date started = null;
    private Date completed = null;
    private String message;
    private Map<String, JobVariable> variables;

    public JobInfo(UUID jobId, UUID diagramId, String diagramName, Mode mode){
        this.jobId = jobId;
        this.diagramId = diagramId;
        this.diagramName = diagramName;
        this.mode = mode;
        this.state = JobStates.CREATED;
        variables = new HashMap<String, JobVariable>();
    }

    public JobInfo(JobInfo jobInfo){
        this(jobInfo.getJobId(), jobInfo.getDiagramId(), jobInfo.getDiagramName(), jobInfo.getMode());

        this.state = jobInfo.getState();
        this.started = jobInfo.getStarted();
        this.completed = jobInfo.getCompleted();
        this.message = jobInfo.getMessage();

        for(JobVariable jobVariable : jobInfo.variables.values())
            variables.put(jobVariable.getKey(), new JobVariable(jobVariable));
    }

    public UUID getJobId() { return this.jobId; }

    public UUID getDiagramId() { return this.diagramId; }

    public String getDiagramName() { return this.diagramName; }

    public Mode getMode() { return this.mode; }

    public JobStates getState() { return this.state; }

    public void setState(JobStates state) { this.state = state; }

    public boolean isDone() {

        boolean done;
        switch(this.state){
            case COMPLETED:
            case STOPPED:
            case FAILED:
                done = true;
                break;
            default:
                done = false;
        }

        return done;
    }

    public Date getStarted() { return this.started; }

    public void setStarted(Date date) { this.started = date; }

    public Date getCompleted() { return this.completed; }

    public void setCompleted(Date date) { this.completed = date; }

    public String getMessage() { return this.message; }

    public void setMessage(String message) { this.message = message; }

    public void addVariable(String key, String value){

        JobVariable variable = getOrCreateJobVariable(key);
        variable.add(value);
    }

    public String lastVariableValue(String key){

        String value = null;
        JobVariable variable = getJobVariable(key);
        if (variable != null)
            value = variable.getLast();

        return value;
    }

    public List<String> listVariableValues(String key){

        JobVariable variable = getOrCreateJobVariable(key);
        return variable.getValues();
    }

    @Override
    public String toString(){
        return String.format("%s (%s): %s", this.diagramName, this.mode.toString(), this.getState().toString());
    }

    private JobVariable getJobVariable(String key){

        JobVariable variable = null;
        if (variables.containsKey(key)){
            variable = variables.get(key);
        }

        return variable;
    }

    private JobVariable getOrCreateJobVariable(String key){

        JobVariable variable = this.getJobVariable(key);
        if (variable == null){
            variable = new JobVariable(key);
            variables.put(key, variable);
        }

        return variable;
    }
}
