package emr.analytics.models.messages;

import java.util.UUID;

public class BlockStatus {
    private UUID id = null;
    private int state = 0;

    private BlockStatus() {}

    public BlockStatus(UUID id, int state) { this.id = id; this.state = state; }

    public UUID getId()
    {
        return this.id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
