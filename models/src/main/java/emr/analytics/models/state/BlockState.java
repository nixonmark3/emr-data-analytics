package emr.analytics.models.state;

import java.util.UUID;

public class BlockState {

    private UUID id = null;
    private String friendlyName = null;
    private int state = 0;

    private BlockState () {}

    public BlockState(UUID id, String friendlyName, int state) {
        this.id = id;
        this.friendlyName = friendlyName;
        this.state = state;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public void setState(int state) {
        this.state = state;
    }
}
