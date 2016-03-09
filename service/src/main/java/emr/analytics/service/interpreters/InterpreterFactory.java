package emr.analytics.service.interpreters;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;

import java.util.Properties;

public class InterpreterFactory {

    public static Interpreter get(String name,
                                  InterpreterNotificationHandler notificationHandler,
                                  TargetEnvironments targetEnvironment,
                                  Mode mode,
                                  Properties properties) throws InterpreterException {

        Interpreter interpreter;
        switch(targetEnvironment){

            case PYSPARK:

                if (mode == Mode.ONLINE)
                    interpreter = new PySparkStreamingInterpreter(name, notificationHandler, properties);
                else
                    interpreter = new PySparkInterpreter(name, notificationHandler, properties);

                break;

            case PYTHON:

                interpreter = new PythonInterpreter(notificationHandler, properties);
                break;

            default:
                throw new InterpreterException(String.format("The specified target environment, %s, is not supported.", targetEnvironment.toString()));
        }

        return interpreter;
    }
}
