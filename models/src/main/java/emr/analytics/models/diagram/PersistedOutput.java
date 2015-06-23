package emr.analytics.models.diagram;

import java.util.UUID;

public class PersistedOutput {
    public UUID id;
    public String name;
    public String type;

    private PersistedOutput(){ }

    public PersistedOutput(UUID id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public UUID getId(){ return this.id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }
}
