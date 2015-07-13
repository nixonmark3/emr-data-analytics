package models.project;

import emr.analytics.models.messages.BaseMessage;
import emr.analytics.models.messages.JobInfo;
import emr.analytics.models.messages.JobStates;

import java.io.Serializable;
import java.util.UUID;

public class EvaluationStatus extends BaseMessage {

    private UUID jobId;
    private UUID diagramId;
    private JobStates state;
    private String blockId;
    private int blockState;

    public EvaluationStatus(JobInfo info){
        super("evaluationStatus");

        this.jobId = info.getJobId();
        this.diagramId = info.getDiagramId();
        this.state = info.getState();

        String progress = info.lastVariableValue("STATE");
        if (progress != null){

            String[] blockInfo = progress.split(",");
            if (blockInfo.length == 2){
                this.blockId = blockInfo[0];
                this.blockState = Integer.parseInt(blockInfo[1]);
            }
        }
    }

    public UUID getJobId() { return this.jobId; }

    public UUID getDiagramId() { return this.diagramId; }

    public JobStates getState() { return this.state; }

    public String getBlockId() { return this.blockId; }

    public int getBlockState() { return this.blockState; }
}
