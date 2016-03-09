package services;

import emr.analytics.database.DefinitionsRepository;
import emr.analytics.database.ResultsRepository;
import emr.analytics.diagram.interpreter.*;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.*;
import models.GroupRequest;
import plugins.MongoPlugin;

import java.util.*;

/**
 * Service that handles diagram operations such as compiling, transforming, and grouping
 */
public class CompilerService {

    private static CompilerService _instance;
    private Map<String, Definition> definitions;

    public CompilerService(){

        MongoPlugin plugin = MongoPlugin.getPlugin();
        DefinitionsRepository repository = new DefinitionsRepository(plugin.getConnection());

        definitions = repository.toMap();
    }

    /**
     * Singleton pattern - retrieve / create the CompilerService instance
     * @return a CompilerService instance
     */
    public static CompilerService getInstance() {

        if(_instance == null) {
            synchronized (AnalyticsService.class) {
                _instance = new CompilerService();
            }
        }
        return _instance;
    }

    /**
     * Compile the specified diagram
     * @param diagram: diagram to be compiled
     * @return resulting source code
     */
    public String compile(Diagram diagram){

        TargetCompiler compiler;
        switch(diagram.getTargetEnvironment()){
            case PYTHON:
                compiler = new PythonCompiler();
                break;
            case PYSPARK:

                compiler = new PySparkCompiler(diagram.getId(), definitions);

                if (diagram.getMode() == Mode.ONLINE){
                    // retrieve the models
                    MongoPlugin plugin = MongoPlugin.getPlugin();
                    ResultsRepository repository = new ResultsRepository(plugin.getConnection());
                    ((PySparkCompiler)compiler).setModels(repository.getPersistedOutputs(diagram));
                }

                break;
            default:
                throw new RuntimeException("The target environment specified is not supported.");
        }

        return compiler.compile(diagram);
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
     * Transforms an offline diagram into an online diagram
     * @param diagram offline diagram
     * @return resulting online diagram
     */
    public Diagram transform(Diagram diagram){
        DiagramTransformer transformer = new DiagramTransformer(definitions);
        return transformer.transform(diagram);
    }
}
