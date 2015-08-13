package emr.analytics.service.jobs;

import emr.analytics.models.messages.JobRequest;

public class JobFactory {

    public static AnalyticsJob get(JobRequest request) throws AnalyticsJobException {

        AnalyticsJob job;
        switch(request.getTargetEnvironment()){

            case PYTHON:
                job = new PythonJob(request);
                break;

            case PYSPARK:
            case SPARK:
                job = new SparkJob(request);
                break;

            default:

                throw new AnalyticsJobException(String.format("The specified target environment, %s, is not supported.", request.getTargetEnvironment().toString()));
        }

        return job;
    }
}
