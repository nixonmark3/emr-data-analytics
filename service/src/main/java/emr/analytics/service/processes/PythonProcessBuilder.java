package emr.analytics.service.processes;

import emr.analytics.service.processes.AnalyticsProcessBuilder;

import java.util.List;

public class PythonProcessBuilder extends AnalyticsProcessBuilder {

    private static final String _command = "python";

    public PythonProcessBuilder(String fileName, List<String> arguments){
        super(fileName, _command, arguments);
    }
}
