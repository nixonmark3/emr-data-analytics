package emr.analytics.service.processes;

import emr.analytics.service.jobs.AnalyticsJob;
import emr.analytics.service.jobs.PythonJob;
import emr.analytics.service.jobs.SparkJob;

public class AnalyticsProcessBuilderFactory {

    public static AnalyticsProcessBuilder get(AnalyticsJob job){

        if(job instanceof PythonJob){
            return new PythonProcessBuilder(job.processArguments());
        }
        else if(job instanceof SparkJob){
            return new SparkSubmitProcessBuilder(job.processArguments());
        }
        else{
            throw new UnsupportedOperationException(String.format("The job type specified, '%s' is not supported.",
                job.getClass().getName()));
        }
    }
}
