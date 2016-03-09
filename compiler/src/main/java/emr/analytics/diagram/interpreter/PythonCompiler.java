package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.WireSummary;

import java.util.*;

public class PythonCompiler extends TargetCompiler {

    // set offline python environmental variables
    private static TargetEnvironments targetEnvironment = TargetEnvironments.PYTHON;

    Set<UUID> compiledBlocks;
    Set<String> importedPackages;

    public PythonCompiler(){
        compiledBlocks = new HashSet<UUID>();
        importedPackages = new HashSet<String>();
    }

    public Set<UUID> getCompiledBlocks(){
        return this.compiledBlocks;
    }

    public SourceBlock getSourceBlock(Block block){
        return this.getSourceBlock(block, null);
    }

    public SourceBlock getSourceBlock(Block block, List<WireSummary> wires){

        SourceBlock sourceBlock;
        switch(block.getDefinitionType()){
            case LOAD:
                sourceBlock = new PythonLoadBlock(block);
                break;

            case GENERAL:
            case MODEL:
                sourceBlock = new PythonBlock(block, wires);
                break;

            default:
                sourceBlock = null;
        }

        return sourceBlock;
    }

    public String compile(Diagram diagram){

        // build an ordered list of blocks to compile
        List<SourceBlock> sourceBlocks = this.getSourceBlocks(diagram);

        StringBuilder packages = new StringBuilder();
        StringBuilder source = new StringBuilder();
        for(SourceBlock sourceBlock : sourceBlocks){

            // build python import statements
            for(Package importPackage : sourceBlock.getPackages()){

                switch(importPackage.getPackageType()){
                    case IMPORT:

                        if(!importedPackages.contains(importPackage.getKey())){
                            packages.append(String.format("from %s import %s\n",
                                    importPackage.getValue(),
                                    importPackage.getValue()));
                            importedPackages.add(importPackage.getKey());
                        }

                        break;
                }
            }

            // append SourceBlock's code
            String code = sourceBlock.getCode(compiledBlocks);
            if (code.length() > 0)
                source.append(code);
        }

        String code = packages.toString() + source.toString();
        return code;
    }
}
