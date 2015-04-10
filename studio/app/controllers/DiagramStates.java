package controllers;

import java.util.UUID;

import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.state.DiagramState;

import org.jongo.MongoCollection;

public class DiagramStates extends ControllerBase
{
    public static void createDiagramState(Diagram diagram, UUID jobId) {
        DiagramState diagramState = DiagramState.Create(diagram, jobId);
        saveDiagramState(diagramState);
    }

    public static DiagramState getDiagramState(UUID jobId) {
        MongoCollection states = getMongoCollection("states");
        for (DiagramState diagramState : states.find().as(DiagramState.class)) {
            if (diagramState.getJobId().equals(jobId)) {
                return diagramState;
            }
        }
        return null;
    }

    public static void saveDiagramState(DiagramState diagramState) {
        MongoCollection states = getMongoCollection("states");
        states.ensureIndex("{name: 1, jobId: 1}", "{unique:true}");
        states.update(String.format("{name: '%s'}", diagramState.getName())).upsert().with(diagramState);
    }
}
