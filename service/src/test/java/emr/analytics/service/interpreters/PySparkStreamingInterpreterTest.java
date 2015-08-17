package emr.analytics.service.interpreters;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class PySparkStreamingInterpreterTest {

    static TestNotificationHandler notificationHandler;
    static PySparkStreamingInterpreter interpreter;

    public static class TestNotificationHandler implements InterpreterNotificationHandler {

        public final List<InterpreterNotification> notifications;

        public TestNotificationHandler(){
            notifications = new ArrayList<InterpreterNotification>();
        }

        public void send(InterpreterNotification notification){

            notifications.add(notification);
        }

        public InterpreterNotification getNotification(int index){
            return this.notifications.get(index);
        }
    }

    @BeforeClass
    public static void setup() {

        notificationHandler = new TestNotificationHandler();
        interpreter = new PySparkStreamingInterpreter("test", notificationHandler);
        interpreter.start();
    }

    @AfterClass
    public static void teardown() {
        interpreter.stop();
    }

    @Test
    public void testInterpreter(){

        StringBuilder source = new StringBuilder();
        source.append("import time\n");
        source.append("rddQueue = []\n");
        source.append("for i in xrange(5):\n");
        source.append("\trddQueue += [sc.parallelize([j for j in xrange(1, 1001)], 10)]\n");
        source.append("inputStream = ssc.queueStream(rddQueue)\n");
        source.append("mappedStream = inputStream.map(lambda x: (x % 10, 1))\n");
        source.append("inputStream.pprint()\n");
        source.append("ssc.start()\n");
        source.append("time.sleep(5)\n");
        source.append("ssc.stop(stopSparkContext=True, stopGraceFully=True)\n");

        InterpreterResult result = interpreter.interpret(source.toString());

        System.out.println(result.toString());

        Assert.assertEquals(InterpreterResult.State.SUCCESS, result.getState());
    }
}


