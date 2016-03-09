package models;

import emr.analytics.models.definition.TargetEnvironments;

import java.util.UUID;

public class DescribeRequest {

    private UUID diagramId;
    private UUID sessionId;
    private String diagramName;
    private TargetEnvironments targetEnvironment;
    private String[] blockConnections;

    public UUID getDiagramId() { return this.diagramId; }

    public UUID getSessionId() { return this.sessionId; }

    public String getDiagramName() { return this.diagramName; }

    public TargetEnvironments getTargetEnvironment() { return this.targetEnvironment; }

    public String[] getBlockConnections() { return this.blockConnections; }

    public String getCode() {
        String outputCollectionName = "output";

        StringBuilder builder = new StringBuilder();
        for(String blockConnection : this.blockConnections)
            builder.append(String.format("dataGateway.describe(\"%s\", %s[\"%s\"])\n", blockConnection, outputCollectionName, blockConnection));

        return builder.toString();
    }

    private DescribeRequest() {}
}
