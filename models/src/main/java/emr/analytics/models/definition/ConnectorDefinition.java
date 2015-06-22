package emr.analytics.models.definition;

public class ConnectorDefinition {
    private String name = null;
    private String type = null;
    private boolean visible = true;
    private boolean persisted = false;

    private ConnectorDefinition() {}

    public ConnectorDefinition(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public ConnectorDefinition(String name, String type, boolean visible, boolean persisted){
        this(name, type);
        this.visible = visible;
        this.persisted = persisted;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getVisible() { return this.visible; }

    public boolean getPersisted() { return this.persisted; }
}
