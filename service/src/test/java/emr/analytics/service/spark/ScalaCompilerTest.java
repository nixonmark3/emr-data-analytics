package emr.analytics.service.spark;

import emr.analytics.models.interfaces.RuntimeMessenger;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScalaCompilerTest {

    private static final ExecutorService pool = Executors.newFixedThreadPool(1);

    @Test
    public void testRun() throws Exception {

        // initialize the scala code
        String source = "val x = 1 + 1\n"
                + "messenger.send(\"Value\", \"The value is: \" + x.toString)\n";

        // create an instance of the runtime messenger
        ScalaTestMessenger messenger = new ScalaTestMessenger();

        ScalaCompiler compiler = new ScalaCompiler(source, messenger);
        Future<Boolean> future = pool.submit(compiler);

        Boolean result = future.get();
        Assert.assertTrue("The code did not return a true value!", result);

        Assert.assertEquals("Messenger value is not correct!", "The value is: 2", messenger.getLastMessage());
    }

    // The runtime messenger implementation for the scala compiler testing
    public class ScalaTestMessenger implements RuntimeMessenger {
        private String lastMessage = null;

        public void send(String key, String value) {
            this.lastMessage = value;
        }

        public String getLastMessage() { return lastMessage; }
    }
}
