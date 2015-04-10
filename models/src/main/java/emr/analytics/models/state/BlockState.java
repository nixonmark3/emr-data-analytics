package emr.analytics.models.state;

public class BlockState {
    private String name = null;
    private int state = 0;

    private BlockState () {}

    public BlockState(String name, int state) {
        this.name = name;
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

    public void setState(int state) {
        this.state = state;
    }
}
