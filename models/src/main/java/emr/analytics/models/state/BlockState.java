package emr.analytics.models.state;

public class BlockState {

    private String name = null;
    private String friendlyName = null;
    private int state = 0;

    private BlockState () {}

    public BlockState(String name, String friendlyName, int state) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
