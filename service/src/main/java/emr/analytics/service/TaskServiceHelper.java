package emr.analytics.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TaskServiceHelper {

    /**
     * Returns the specified environmental variable or a default value
     * @param name the name of the environmental variable
     * @param defaultValue an optional default value
     * @return the value of environmental variable
     */
    public static String getEnvVariable(String name, String defaultValue){
        String value = getEnvVariable(name);
        if (value == null)
            value = defaultValue;

        return value;
    }

    public static String getEnvVariable(String name){
        return System.getenv(name);
    }
}
