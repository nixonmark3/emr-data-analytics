package emr.analytics.models.definition;

import emr.analytics.models.diagram.Diagram;

import java.util.ArrayList;
import java.util.List;

public class ParameterSourceRequest {

    private String name = null;
    private List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
    private Diagram diagram = null;

    private ParameterSourceRequest(){ }

    public ParameterSourceRequest(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParameterDefinition> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterDefinition> parameters) {
        this.parameters = parameters;
    }

    public Diagram getDiagram() {
        return diagram;
    }

    public void setDiagram(Diagram diagram) {
        this.diagram = diagram;
    }
}
