package models;

import emr.analytics.models.definition.TargetEnvironments;

import java.util.*;

public class CollectRequest {

    private UUID diagramId;
    private UUID sessionId;
    private String diagramName;
    private TargetEnvironments targetEnvironment;
    private FeatureRequest[] features;

    public UUID getDiagramId() { return this.diagramId; }

    public UUID getSessionId() { return this.sessionId; }

    public String getDiagramName() { return this.diagramName; }

    public TargetEnvironments getTargetEnvironment() { return this.targetEnvironment; }

    public FeatureRequest[] getFeatures() { return this.features; }

    public String getCode() {
        String outputCollectionName = "output";

        // group features by data source since each data source will be a call to the datagateway
        Map<String, List<String>> featuresBySource = new HashMap<String, List<String>>();
        for(FeatureRequest featureRequest : this.features){

            List<String> featureNames;
            if(featuresBySource.containsKey(featureRequest.getSourceName())) {
                featureNames = featuresBySource.get(featureRequest.getSourceName());
            }
            else {
                featureNames = new ArrayList<String>();
                featuresBySource.put(featureRequest.getSourceName(), featureNames);
            }
            featureNames.add(featureRequest.getName());
        }

        // build source code
        StringBuilder code = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : featuresBySource.entrySet()) {
            code.append(String.format("dataGateway.select(['%s'], %s[\"%s\"])\n",
                    String.join("', '", entry.getValue()),
                    outputCollectionName,
                    entry.getKey()));
        }

        return code.toString();
    }

    private CollectRequest() {}

    public static class FeatureRequest {
        private String sourceName;
        private String name;

        public String getSourceName() { return this.sourceName; }

        public String getName() { return this.name; }

        private FeatureRequest(){}
    }
}
