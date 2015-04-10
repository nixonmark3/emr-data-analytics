package emr.analytics.models.messages;

public class BlockStatus {
    private String blockName = "";
    private int state = 0;

    private BlockStatus() {}

    public BlockStatus(String blockName, int state)
    {
        this.blockName = blockName;
        this.state = state;
    }

    public String getBlockName()
    {
        return blockName;
    }

    public void setBlockName(String blockName)
    {
        this.blockName = blockName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
