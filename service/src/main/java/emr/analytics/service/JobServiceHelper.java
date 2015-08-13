package emr.analytics.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JobServiceHelper {

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
