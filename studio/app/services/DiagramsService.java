package services;

import com.mongodb.WriteResult;
import emr.analytics.diagram.interpreter.*;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.*;
import emr.analytics.models.messages.JobRequest;
import models.project.GroupRequest;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import plugins.MongoDBPlugin;

import java.util.*;

public class DiagramsService {

    private static DiagramsService _instance;
    private HashMap<String, Definition> definitions;

    public DiagramsService(){
        definitions = DefinitionsService.getDefinitionsMap();
    }

    public static DiagramsService getInstance() {

        if(_instance == null) {
            synchronized (AnalyticsService.class) {
                _instance = new DiagramsService();
            }
        }
        return _instance;
    }

    /**
     * Transform the specified diagram into a source code string
     * @param diagram
     * @return source code string
     * @throws CompilerException
     */
    public CompiledDiagram compile(Diagram diagram) throws CompilerException {

        HashMap<String, String> outputs = this.getPersistedOutputs(diagram);
        DiagramCompiler compiler = new DiagramCompiler(definitions);

        return compiler.compile(diagram, outputs);
    }

    /**
     * Group the specified collection of blocks
     * @param request - the group request { group name, parent diagram, and list of block ids to group }
     * @return diagram containing the new group
     */
    public Diagram group(GroupRequest request){

        return DiagramOperations.group(request.getName(), request.getDiagram(), request.getBlocks());
    }

    /**
     * Create a job request for the specified diagram
     * @param diagram
     * @return A job request
     * @throws CompilerException
     */
    public JobRequest getJobRequest(Diagram diagram) throws CompilerException {

        CompiledDiagram compiledDiagram = this.compile(diagram);

        JobRequest request = new JobRequest(diagram.getId(),
            diagram.getMode(),
            diagram.getTargetEnvironment(),
            diagram.getName(),
            compiledDiagram.getSource(),
            compiledDiagram.getMetaData());

        return request;
    }

    /**
     * Transforms an offline diagram into an online diagram
     * @param diagram offline diagram
     * @return resulting online diagram
     */
    public Diagram transform(Diagram diagram){

        DiagramTransformer transformer = new DiagramTransformer(definitions);
        return transformer.transform(diagram);
    }


    public UUID save(DiagramContainer diagramContainer) {

        UUID diagramId = null;
        try {

            MongoCollection dbDiagrams = this.getDiagramsDbCollection();

            if (dbDiagrams != null) {

                Diagram offline = diagramContainer.getOffline();
                Diagram online = diagramContainer.getOnline();

                if (offline != null) {

                    if (online != null) {

                        offline.setParameterOverrides(this.getParameterOverrides(online));
                    }

                    // create a diagram id for new diagrams
                    diagramId = offline.getId();
                    if (diagramId == null){
                        diagramId = UUID.randomUUID();
                        offline.setId(UUID.randomUUID());
                    }

                    dbDiagrams.ensureIndex("{name: 1}", "{unique:true}");

                    WriteResult update = dbDiagrams.update(String.format("{name: '%s'}", offline.getName())).upsert().with(offline);
                }
            }
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }

        return diagramId;
    }

    private MongoCollection getDiagramsDbCollection() {

        MongoCollection diagrams = null;

        MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

        if (mongoPlugin != null) {

            Jongo db = mongoPlugin.getJongoDBInstance(mongoPlugin.getStudioDatabaseName());

            if (db != null) {

                diagrams = db.getCollection("diagrams");
            }
        }

        return diagrams;
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

    private HashMap<String, String> getPersistedOutputs(Diagram diagram){

        HashMap<String, String> persistedOutputs = new HashMap<>();
        for(PersistedOutput persistedOutput : diagram.getPersistedOutputs()){

            BlockResultsService blockResultsService = new BlockResultsService();
            String output = blockResultsService.getOutput(persistedOutput.getId(), persistedOutput.getName());
            String variableName = String.format("%s_%s", persistedOutput.getId(), persistedOutput.getName());

            persistedOutputs.put(variableName, output);
        }

        return persistedOutputs;
    }
}
