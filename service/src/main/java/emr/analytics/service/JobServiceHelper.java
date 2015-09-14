package emr.analytics.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JobServiceHelper {

    /**
     * Returns the specified environmental variable or a default value
     * @param name the name of the environmental variable
     * @param defaultValue an optional default value
     * @return the value of environmental variable
     */
    public static String getEnvVariable(String name, String defaultValue){
        String value = System.getenv(name);
        if (value == null)
            value = defaultValue;

        return value;
    }

    public static Properties loadProperties(String name){

        Properties properties = new Properties();

        String fileName = String.format("conf/%s.properties", name);
        try (InputStream stream = JobServiceHelper.class.getClassLoader().getResourceAsStream(fileName)){
            properties.load(stream);
        }
        catch(IOException ex){
            throw new JobServiceException(String.format("Unable to load specified properties: %s.", name));
        }

        return properties;
    }
}
