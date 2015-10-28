package emr.analytics.service.interpreters;

import emr.analytics.models.messages.Describe;
import emr.analytics.models.messages.Features;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class PythonInterpreterTest {

    static TestNotificationHandler notificationHandler;
    static PythonInterpreter interpreter;

    public static class TestNotificationHandler implements InterpreterNotificationHandler {

        public final List<InterpreterNotification> notifications;

        public TestNotificationHandler(){
            notifications = new ArrayList<InterpreterNotification>();
        }

        public void notify(InterpreterNotification notification){

            notifications.add(notification);
        }

        public void describe(Describe describe){}

        public void collect(Features features){}

        public InterpreterNotification getNotification(int index){
            return this.notifications.get(index);
        }
    }

    @BeforeClass
    public static void setup() {

        notificationHandler = new TestNotificationHandler();
        interpreter = new PythonInterpreter(notificationHandler);
        interpreter.start();
    }

    @AfterClass
    public static void teardown() {
        interpreter.stop();
    }

    @Test
    public void testInterpreter(){

        InterpreterResult result = interpreter.interpret("x = 1 + 1\nprint x");
        Assert.assertEquals(InterpreterResult.State.SUCCESS, result.getState());
        Assert.assertEquals("2", result.getMessage().trim());

        result = interpreter.interpret("interpreter.onNotify('VAL', str(x))");
        Assert.assertEquals(InterpreterResult.State.SUCCESS, result.getState());
        Assert.assertEquals("", result.getMessage().trim());

        InterpreterNotification notification = notificationHandler.getNotification(0);
        Assert.assertEquals("VAL", notification.getKey());
        Assert.assertEquals("2", notification.getValue().trim());
    }
}