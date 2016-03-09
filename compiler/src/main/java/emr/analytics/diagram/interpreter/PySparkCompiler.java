package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.*;

import java.util.*;

public class PySparkCompiler extends TargetCompiler {

    private static TargetEnvironments targetEnvironment = TargetEnvironments.PYSPARK;

    private UUID diagramId;
    private Map<String, Definition> definitions;
    private Map<String, String> models;
    private Properties properties;

    public PySparkCompiler(UUID diagramId, Map<String, Definition> definitions, Properties properties){
        this.diagramId = diagramId;
        this.definitions = definitions;
        this.properties = properties;
        this.models = null;
    }

    public PySparkCompiler(UUID diagramId, Map<String, Definition> definitions){
        this(diagramId, definitions, new Properties(System.getProperties()));
    }

    public void setModels(Map<String, String> models){
        this.models = models;
    }

    @Override
    public SourceBlock getSourceBlock(Block block, List<WireSummary> wires){

        SourceBlock sourceBlock;
        switch(block.getDefinitionType()){
            case LOAD:
                sourceBlock = new PySparkLoadBlock(block);
                break;

            case STREAM:
                sourceBlock = new PySparkStreamBlock(block);
                break;

            case OUTPUT:
                sourceBlock = new PySparkOutputBlock(this.diagramId, block, wires, properties);
                break;

            case GENERAL:
            case MODEL:

                Definition definition = this.definitions.get(block.getDefinition());
                sourceBlock = new PySparkBlock(block, wires, definition);
                break;

            default:
                sourceBlock = null;
        }

        return sourceBlock;
    }

    @Override
    public String compile(Diagram diagram){

        Set<String> importedPackages = new HashSet<String>();

        // build an ordered list of blocks to compile
        List<SourceBlock> sourceBlocks = this.getSourceBlocks(diagram);

        // iterate over the source blocks to capture the packages to import and the
        // block source code
        StringBuilder packages = new StringBuilder();
        StringBuilder source = new StringBuilder();
        for(SourceBlock sourceBlock : sourceBlocks){

            // build python import statements
            for(Package importPackage : sourceBlock.getPackages()){

                switch(importPackage.getPackageType()){
                    case IMPORT:

                        if(!importedPackages.contains(importPackage.getKey())){
                            packages.append(String.format("import %s\n",
                                    importPackage.getValue()));
                            importedPackages.add(importPackage.getKey());
                        }
                        break;

                    case FUNCTION:

                        if(!importedPackages.contains(importPackage.getKey())){
                            packages.append(importPackage.getValue());
                            importedPackages.add(importPackage.getKey());
                        }
                        break;
                }
            }

            // append SourceBlock's code
            String code = sourceBlock.getCode();
            if (code.length() > 0)
                source.append(code);
        }

        // append the models
        if (this.models != null){
            this.models.forEach((key, value) -> {
                String name = CompileHelper.createVariableName(key);
                packages.append(String.format("%s = [%s]\n", name, value));
            });
        }

        // build source code suffix based on diagram mode
        String suffix;
        if (diagram.getMode() == Mode.ONLINE)
            suffix = "ssc.start()\nssc.awaitTermination()\n\n";
        else
            suffix = "";

        // concatenate source code segments
        String code = String.format("%s\n%s\n%s", packages.toString(),
                source.toString(),
                suffix);

        return code;
    }
}
