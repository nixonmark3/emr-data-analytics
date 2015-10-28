package models;

import java.util.UUID;

public class CollectRequest {

    private UUID diagramId;
    private String diagramName;
    private String dataFrameName;
    private String[] features;

    public String getDataFrameName() { return this.dataFrameName; }

    public UUID getDiagramId() { return this.diagramId; }

    public String getDiagramName() { return this.diagramName; }

    public String[] getFeatures() { return this.features; }

    public String getCode() {

        return String.format("features = %s.select('%s')\ndataGateway.collect(features)",
                this.dataFrameName,
                String.join("', '", this.features));
    }

    private CollectRequest() {}
}
