package emr.analytics.service.interpreters;

import emr.analytics.models.messages.Describe;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class PySparkInterpreterTest {

    static TestNotificationHandler notificationHandler;
    static PySparkInterpreter interpreter;

    public static class TestNotificationHandler implements InterpreterNotificationHandler {

        public final List<InterpreterNotification> notifications;

        public TestNotificationHandler(){
            notifications = new ArrayList<InterpreterNotification>();
        }

        public void notify(InterpreterNotification notification){

            notifications.add(notification);
        }

        public void describe(Describe describe){

        }

        public InterpreterNotification getNotification(int index){
            return this.notifications.get(index);
        }
    }

    @BeforeClass
    public static void setup() {

        notificationHandler = new TestNotificationHandler();
        interpreter = new PySparkInterpreter("test", notificationHandler);
        interpreter.start();
    }

    @AfterClass
    public static void teardown() {
        interpreter.stop();
    }

    @Test
    public void testInterpreter(){

        StringBuilder source = new StringBuilder();
        source.append("data = [1, 2, 3, 4]\n");
        source.append("distData = sc.parallelize(data)\n");
        source.append("result = distData.map(lambda x: x * 2).sum()\n");
        source.append("print result\n");

        InterpreterResult result = interpreter.interpret(source.toString());

        System.out.println(result.getMessage());

        Assert.assertEquals(InterpreterResult.State.SUCCESS, result.getState());
        Assert.assertEquals("20", result.getMessage().trim());

        result = interpreter.interpret("interpreter.onNotify('VAL', str(result))");
        Assert.assertEquals(InterpreterResult.State.SUCCESS, result.getState());
        Assert.assertEquals("", result.getMessage().trim());

        InterpreterNotification notification = notificationHandler.getNotification(0);
        Assert.assertEquals("VAL", notification.getKey());
        Assert.assertEquals("20", notification.getValue().trim());
    }
}