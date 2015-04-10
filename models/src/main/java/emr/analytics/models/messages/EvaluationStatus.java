package emr.analytics.models.messages;

import java.util.ArrayList;
import java.util.UUID;

public class EvaluationStatus {
    private int state = 0;
    private UUID jobId = null;
    private ArrayList<BlockStatus> blockStatusList = new ArrayList<BlockStatus>();

    private EvaluationStatus() {}

    public EvaluationStatus(UUID jobId, int state) {
        this.jobId = jobId;
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public UUID getJobId()
    {
        return jobId;
    }

    public void setJobId(UUID jobId)
    {
        this.jobId = jobId;
    }

    public ArrayList<BlockStatus> getBlockStatusList()
    {
        return blockStatusList;
    }

    public void setBlockStatusList(ArrayList<BlockStatus> blockStatusList)
    {
        this.blockStatusList = blockStatusList;
    }
}
