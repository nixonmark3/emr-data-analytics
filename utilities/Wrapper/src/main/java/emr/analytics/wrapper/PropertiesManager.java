package emr.analytics.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {

    private static PropertiesManager _instance;
    private Properties properties;

    public PropertiesManager(){

        this.properties = new Properties(System.getProperties());
        loadProperties(this.properties, "app");

        String propertiesFilePath = this.properties.getProperty("analytics.configuration");
        if (propertiesFilePath != null)
            loadPropertiesFile(this.properties, propertiesFilePath);
    }

    public static PropertiesManager getInstance() {

        if(_instance == null) {
            synchronized (PropertiesManager.class) {

                if (_instance == null)
                    _instance = new PropertiesManager();
            }
        }
        return _instance;
    }

    public Properties getProperties(){
        return this.properties;
    }

    private void loadProperties(Properties properties, String name){

        String fileName = String.format("%s.properties", name);
        try (InputStream stream = PropertiesManager.class.getClassLoader().getResourceAsStream(fileName)){
            properties.load(stream);
        }
        catch(IOException ex){
            throw new RuntimeException(String.format("Unable to load specified properties: %s.", name));
        }
    }

    private void loadPropertiesFile(Properties properties, String path){

        File file = new File(path);
        if (file.isFile() && file.canRead()) {

            try(FileInputStream stream = new FileInputStream(file)) {
                properties.load(stream);
            } catch (IOException ex) {
                throw new RuntimeException(String.format("Unable to load specified properties: %s.", path));
            }
        }
    }
}
