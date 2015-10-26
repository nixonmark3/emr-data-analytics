package models;

import java.util.UUID;

public class InterpreterRequest {

    private UUID diagramId;
    private String diagramName;
    private String source;

    public UUID getDiagramId() { return this.diagramId; }

    public String getDiagramName() { return this.diagramName; }

    public String getSource() { return this.source; }

}
