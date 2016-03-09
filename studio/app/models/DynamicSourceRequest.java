package models;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.ParameterDefinition;
import emr.analytics.models.definition.TargetEnvironments;

import java.util.UUID;

public class DynamicSourceRequest {

    private UUID diagramId;
    private UUID sessionId;
    private String diagramName;
    private Mode mode;
    private TargetEnvironments targetEnvironment;
    private ParameterDefinition parameter;

    public DynamicSourceRequest(){}

    public UUID getDiagramId() { return this.diagramId; }

    public UUID getSessionId() { return this.sessionId; }

    public String getDiagramName() { return this.diagramName; }

    public Mode getMode() { return this.mode; }

    public TargetEnvironments getTargetEnvironment() { return this.targetEnvironment; }

    public ParameterDefinition getParameter() { return this.parameter; }
}
