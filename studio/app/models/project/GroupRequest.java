package models.project;

import emr.analytics.models.diagram.Diagram;

public class GroupRequest {

    private String name;
    private Diagram diagram;
    private String[] blocks;

    public String getName(){
        return name;
    }

    public Diagram getDiagram(){
        return diagram;
    }

    public String[] getBlocks(){
        return blocks;
    }

    private GroupRequest(){ }
}
