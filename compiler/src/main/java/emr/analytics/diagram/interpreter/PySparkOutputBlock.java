package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.WireSummary;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class PySparkOutputBlock extends SourceBlock {

    private final String functionName = "output";

    private UUID diagramId;
    private Properties properties;

    public PySparkOutputBlock(UUID diagramId, Block block, List<WireSummary> wires, Properties properties){
        super(TargetEnvironments.PYSPARK, block, wires);

        this.diagramId = diagramId;
        this.properties = properties;
    }

    @Override
    public String getCode(Set<UUID> compiled){
        String objectName = CompileHelper.buildArguments(new String[] { "input:in" },
            this.block,
            this.wires);
        return String.format("%s.foreachRDD(lambda rdd: %s(rdd))\n", objectName, functionName);
    }

    @Override
    public Package[] getPackages(){

        String broker = properties.getProperty("kafka.producer", "localhost:9092");
        String topic = properties.getProperty("kafka.online.topic", "ONLINE");

        String output = "\nfrom kafka import KeyedProducer, KafkaClient\n"

                + String.format("kafka = KafkaClient(\"%s\")\n", broker)
                + "producer = KeyedProducer(kafka)\n\n"
                + String.format("def %s(rdd):\n", functionName)
                + "\tresults = rdd.collect()\n"
                + "\tfor result in results:\n"
                + "\t\tfor item in result:\n"
                + String.format("\t\t\tproducer.send_messages(\"%s\", \"%s\".encode(\"UTF-8\"), str(item).encode(\"UTF-8\"))\n\n", topic, diagramId.toString());

        return new Package[] {
                new Package(Package.PackageType.FUNCTION,
                        "PYSPARK_OUTPUT_BLOCK",
                        output)
        };
    }
}
