package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class PySparkStreamBlock extends StreamBlock {

    private final String packageName = "StreamingSources";
    private final String functionName = "createQueueStream";

    public PySparkStreamBlock(Block block, Properties properties){
        super(TargetEnvironments.PYSPARK, block);
    }

    public PySparkStreamBlock(Block block){
        this(block, null);
    }

    @Override
    public String getCode(Set<UUID> compiled){

        String variableName = String.format("%s_%s",
            CompileHelper.createVariableName(block.getId().toString()),
            block.getOutputConnectors().get(0).getName());

        String code;
        switch(this.config.getSourceType()){
            case FILES:
                String path = this.config.getDataSource().getPath();
                code = String.format("%s = %s.%s(ssc, \"%s\")\n",
                        variableName,
                        packageName,
                        functionName,
                        path);
                break;
            default:
                throw new RuntimeException("Streaming source type specified is not supported.");
        }

        return code;
    }

    @Override
    public Package[] getPackages(){

        Package[] packages;
        switch(this.config.getSourceType()){
            case FILES:
                packages = new Package[] {
                        new Package(Package.PackageType.IMPORT, packageName, packageName)
                };
                break;
            default:
                throw new RuntimeException("Streaming source type specified is not supported.");
        }

        return packages;
    }


}
