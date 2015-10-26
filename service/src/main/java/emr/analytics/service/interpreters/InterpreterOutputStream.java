package emr.analytics.service.interpreters;

import org.apache.commons.exec.LogOutputStream;

public class InterpreterOutputStream extends LogOutputStream {

    private final InterpreterNotificationHandler notificationHandler;

    public InterpreterOutputStream(InterpreterNotificationHandler notificationHandler, OutputLevel level){
        super(level.getValue());
        this.notificationHandler = notificationHandler;
    }

    @Override
    protected void processLine(String line, int level) {
        this.notificationHandler.notify(new InterpreterNotification(OutputLevel.fromValue(level).toString(), line));
    }

    public enum OutputLevel {
        OUT(0), ERROR(1), UNKNOWN(2);

        private final int value;
        private static final OutputLevel[] levels = OutputLevel.values();

        private OutputLevel(int value){
            this.value = value;
        }

        public int getValue(){
            return this.value;
        }

        public static OutputLevel fromValue(int value){

            for(OutputLevel level : levels)
                if (level.getValue() == value)
                    return level;

            return OutputLevel.UNKNOWN;
        }
    }
}
