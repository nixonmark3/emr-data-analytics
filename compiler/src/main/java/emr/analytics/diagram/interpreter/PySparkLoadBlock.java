package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class PySparkLoadBlock extends LoadBlock {

    private Properties properties;

    public PySparkLoadBlock(Block block, Properties properties){
        super(TargetEnvironments.PYSPARK, block);

        // if properties were not provided, load the target envrionment's properties
        this.properties = properties;
        if (this.properties == null){
            this.properties = new Properties();
            CompileHelper.loadProperties(targetEnvironment, this.properties);
        }
    }

    public PySparkLoadBlock(Block block){
        this(block, null);
    }

    @Override
    public String getCode(Set<UUID> compiled){

        Source source = this.config.getSource();
        Parse parse = this.config.getParse();
        List<Transformation> transformations = this.config.getTransformations();

        // validate the load configuration
        if (source.getDataSources().size() == 0)
            throw new CompilerException("Unable to load data, a file has not been specified.");

        // generate names for rdd and output based on block name
        String rddName = String.format("%s_rdd", block.getName());
        String outputName = String.format("%s_%s", block.getName(), block.getOutputConnectors().get(0).getName());

        StringBuilder builder = new StringBuilder();

        if (parse.getParseType() == ParseTypes.SEPARATED_VALUES){

            builder.append("import PySparkCSV\n");

            builder.append(String.format("%s = %s.textFile('file://%s')\n",
                    rddName,
                    properties.getProperty("sparkContext"),
                    source.getDataSources().get(0).getPath()));

            builder.append(String.format("%s = PySparkCSV.csvToDataFrame(%s, %s)\n",
                    outputName,
                    properties.getProperty("sqlContext"),
                    rddName));

            builder.append(String.format("%s.persist()\n", outputName));
        }

        builder.append(String.format("dataGateway.describe(%s)\n", outputName));

        return builder.toString();
    }

    @Override
    public Package[] getPackages(){
        return new Package[] {};
    }
}
