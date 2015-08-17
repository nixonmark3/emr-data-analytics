package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.Diagram;

import java.util.HashMap;

public class DiagramCompiler {

    private HashMap<String, Definition> definitions;

    public DiagramCompiler(HashMap<String, Definition> definitions) {

        this.definitions = definitions;
    }

    /**
     *
     * @param diagram
     * @param models
     * @return
     * @throws CompilerException
     */
    public String compile(Diagram diagram, HashMap<String, String> models) throws CompilerException {

        TargetCompiler compiler;
        switch (diagram.getTargetEnvironment()) {

            case SPARK:
            case PYSPARK:
                compiler = new PySparkCompiler(definitions, diagram, models);
                break;

            case PYTHON:
                compiler = new PythonCompiler(definitions, diagram);
                break;

            default:
                throw new CompilerException("The compiler does not support the specified target environment.");
        }

        return compiler.compile();
    }

    /**
     *
     * @param diagram
     * @return
     * @throws CompilerException
     */
    public String compile(Diagram diagram) throws CompilerException {
        return this.compile(diagram, new HashMap<String, String>());
    }
}
