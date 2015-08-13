package emr.analytics.service.interpreters;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;

public class InterpreterFactory {

    public static Interpreter get(String name,
                                  InterpreterNotificationHandler notificationHandler,
                                  TargetEnvironments targetEnvironment,
                                  Mode mode) throws InterpreterException {

        Interpreter interpreter;
        switch(targetEnvironment){

            case PYSPARK:

                if (mode == Mode.ONLINE)
                    interpreter = new PySparkStreamingInterpreter(name, notificationHandler);
                else
                    interpreter = new PySparkInterpreter(name, notificationHandler);

                break;

            default:
                throw new InterpreterException(String.format("The specified target environment, %s, is not supported.", targetEnvironment.toString()));
        }

        return interpreter;
    }
}
