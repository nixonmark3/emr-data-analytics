package models;

import emr.analytics.models.messages.AnalyticsTask;
import emr.analytics.models.messages.OutputMessage;

import java.io.Serializable;
import java.util.UUID;

public class EvaluationStatus extends OutputMessage {

    private UUID diagramId;
    private String message;
    private String blockId;
    private int blockState;

    public EvaluationStatus(AnalyticsTask task){
        super("evaluationStatus");

        this.diagramId = task.getDiagramId();
        // this.message = task.getMessage();

        /*String progress = info.lastVariableValue("STATE");
        if (progress != null){

            String[] blockInfo = progress.split(",");
            if (blockInfo.length == 2){
                this.blockId = blockInfo[0];
                this.blockState = Integer.parseInt(blockInfo[1]);
            }
        }*/
    }

    public UUID getDiagramId() { return this.diagramId; }

    public String getMessage() { return this.message; }

    public String getBlockId() { return this.blockId; }

    public int getBlockState() { return this.blockState; }
}
