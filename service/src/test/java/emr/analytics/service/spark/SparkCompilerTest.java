package emr.analytics.service.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SparkCompilerTest {

    private static final ExecutorService pool = Executors.newFixedThreadPool(1);

    @Test
    public void testRun() throws Exception {

        // initialize the scala code
        String source = "val data = Array(1, 2, 3, 4)\n"
                + "val distData = sc.parallelize(data)\n"
                + "val result = distData.map(x => x * 2).sum().toInt\n"
                + "messenger.send(\"Value\", \"The value is: \" + result.toString)\n";

        // create an instance of the runtime messenger
        SparkTestMessenger messenger = new SparkTestMessenger();

        SparkConf conf = new SparkConf()
                .setMaster("local[2]")
                .setAppName("SparkCompilerTest");
        SparkContext sc = new SparkContext(conf);

        SparkCompiler compiler = new SparkCompiler(sc, source, messenger);
        Future<Boolean> future = pool.submit(compiler);

        Boolean result = future.get();
        Assert.assertTrue("The code did not return a true value!", result);

        Assert.assertEquals("Messenger value is not correct!", "The value is: 20", messenger.getLastMessage());

        sc.stop();
    }

    // The runtime messenger implementation for the scala compiler testing
    public class SparkTestMessenger implements RuntimeMessenger {
        private String lastMessage = null;

        public void send(String key, String value) {
            this.lastMessage = value;
        }

        public String getLastMessage() { return lastMessage; }
    }
}