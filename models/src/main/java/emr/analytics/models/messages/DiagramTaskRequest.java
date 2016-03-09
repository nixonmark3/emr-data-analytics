package emr.analytics.models.messages;

import emr.analytics.models.diagram.Diagram;
import java.io.Serializable;
import java.util.UUID;

public class DiagramTaskRequest extends TaskRequest implements Serializable {
    private Diagram diagram;

    public DiagramTaskRequest(Diagram diagram){
        super(null, diagram.getId(), diagram.getMode(), diagram.getTargetEnvironment(), diagram.getName(), "");

        this.diagram = diagram;
    }

    public Diagram getDiagram(){ return this.diagram; }
}
