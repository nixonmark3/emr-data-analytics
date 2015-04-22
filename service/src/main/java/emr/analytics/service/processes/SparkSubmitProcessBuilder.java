package emr.analytics.service.processes;

import emr.analytics.service.processes.AnalyticsProcessBuilder;

import java.util.List;

public class SparkSubmitProcessBuilder extends AnalyticsProcessBuilder {

    private static final String _command = "bin/spark-submit";
    private static final String _evCommandPrefix = "SPARK_HOME";

    public SparkSubmitProcessBuilder(List<String> arguments){
        super(_command, arguments, _evCommandPrefix);
    }
}

