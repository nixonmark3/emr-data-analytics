package emr.analytics.models.messages;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;

import java.io.Serializable;
import java.util.UUID;

public class JobRequest extends BaseMessage implements Serializable {

    private UUID diagramId;
    private Mode mode;
    private TargetEnvironments targetEnvironment;
    private String diagramName;
    private String source;
    private String metaData;

    public JobRequest(UUID diagramId, Mode mode, TargetEnvironments targetEnvironment, String diagramName, String source, String metaData){
        super("jobRequest");

        this.diagramId = diagramId;
        this.mode = mode;
        this.targetEnvironment = targetEnvironment;
        this.diagramName = diagramName;
        this.source = source;
        this.metaData = metaData;
    }

    public UUID getDiagramId(){ return this.diagramId; }

    public Mode getMode() { return this.mode; }

    public TargetEnvironments getTargetEnvironment() { return this.targetEnvironment; }

    public String getDiagramName(){ return this.diagramName; }

    public String getSource(){ return this.source; }

    public String getMetaData() { return this.metaData; }
}
