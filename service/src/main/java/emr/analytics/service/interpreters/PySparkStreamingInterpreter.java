package emr.analytics.service.interpreters;


import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class PySparkStreamingInterpreter extends PySparkInterpreter implements ExecuteResultHandler {

    protected JavaStreamingContext streamingContext;

    public PySparkStreamingInterpreter(String name, InterpreterNotificationHandler notificationHandler){
        super(name, notificationHandler);

        String path = this.getWorkingDirectory().getAbsolutePath();

        this.sparkContext.addJar("/usr/local/spark/external/kafka-assembly/spark-streaming-kafka-assembly_2.10-1.4.1.jar");
        this.sparkContext.addFile(String.format("%s/Filters.py", path));
        this.sparkContext.addFile(String.format("%s/StreamingSources.py", path));
        this.sparkContext.addFile(String.format("%s/Transformations.py", path));

        // create a streaming context
        this.streamingContext = new JavaStreamingContext(this.sparkContext, Durations.seconds(1));
    }

    @Override
    protected String[] scriptFiles(){
        return new String[] { "python_init", "pyspark_init", "pyspark_streaming_init", "python_eval" };
    }

    @Override
    public void stop(){
        super.stop();

        System.out.println("stopping streaming context.");
        this.streamingContext.stop(true, true);
        System.out.println("stopped streaming context.");
    }

    public JavaStreamingContext getStreamingContext(){ return this.streamingContext; }
}
