package emr.analytics.service.interpreters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Interpreter {

    //
    protected final InterpreterNotificationHandler notificationHandler;

    //
    private final Properties properties;

    /**
     *
     * @param notificationHandler
     */
    public Interpreter(InterpreterNotificationHandler notificationHandler, Properties properties){
        this.notificationHandler = notificationHandler;
        this.properties = properties;
    }

    /**
     *
     * @return
     */
    protected Properties getProperties(){ return this.properties; }

    /**
     *
     * @param source
     * @return
     */
    public abstract InterpreterResult interpret(String source);

    /**
     *
     */
    public abstract void start();

    /**
     *
     */
    public abstract void stop();

    /**
     *
     * @param key
     * @param value
     */
    public void onNotify(String key, String value){
        this.notificationHandler.notify(new InterpreterNotification(key, value));
    }
}

