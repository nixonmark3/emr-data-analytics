package emr.analytics.database;

import com.mongodb.WriteResult;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.DiagramContainer;
import emr.analytics.models.diagram.DiagramRoot;
import emr.analytics.models.diagram.ParameterOverride;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiagramsRepository {

    private static final String collectionName = "diagrams";
    private static final String DIAGRAM_PROJECTION = "{_id: 0, name: 1, description: 1, owner: 1, category: 1, mode: 1, targetEnvironment: 1, version: 1}";
    private static final String SORT_BY_NAME = "{name: 1}";
    private static final String QUERY_BY_UNIQUE_ID = "{name: '%s'}";

    private MongoCollection collection;

    public DiagramsRepository(MongoConnection connection) {
        this.collection = connection.getCollection(collectionName);
    }

    public List<DiagramRoot> all() {

        try {

            List<DiagramRoot> diagrams = new ArrayList<DiagramRoot>();
            MongoCursor<DiagramRoot> query = this.collection.find().projection(DIAGRAM_PROJECTION).sort(SORT_BY_NAME).as(DiagramRoot.class);
            for (DiagramRoot diagram : query)
                diagrams.add(diagram);

            return diagrams;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
    }

    public void delete(String name) {

        try {
            collection.remove(String.format(QUERY_BY_UNIQUE_ID, name));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
    }

    public Diagram get(String name) {

        Diagram diagram;
        try {
            diagram = collection.findOne(String.format(QUERY_BY_UNIQUE_ID, name)).as(Diagram.class);
            ;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }

        return diagram;
    }

    public UUID save(DiagramContainer diagramContainer) {

        try {

            UUID diagramId = null;

            Diagram offline = diagramContainer.getOffline();
            Diagram online = diagramContainer.getOnline();

            if (offline != null) {

                if (offline.hasDefaultName())
                    throw new DatabaseException("Diagrams must be given a new name.");

                if (online != null)
                    offline.setParameterOverrides(this.getParameterOverrides(online));

                // create a diagram id for new diagrams
                diagramId = offline.getId();
                if (diagramId == null) {
                    diagramId = UUID.randomUUID();
                    offline.setId(diagramId);
                }

                this.collection.ensureIndex("{name: 1}", "{unique:true}");

                WriteResult update = this.collection.update(String.format("{name: '%s'}", offline.getName())).upsert().with(offline);
            }

            return diagramId;
        }
        catch(Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
    }

    private List<ParameterOverride> getParameterOverrides(Diagram onlineDiagram) {

        List<ParameterOverride> parameterOverrides = new ArrayList<ParameterOverride>();

        if (onlineDiagram != null) {

            onlineDiagram.getBlocks().stream().forEach((block) -> {

                block.getParameters().stream().forEach((parameter) -> {

                    parameterOverrides.add(new ParameterOverride(block.getId(), parameter.getName(), parameter.getValue()));
                });
            });
        }

        return parameterOverrides;
    }
}
