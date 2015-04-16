package services;

import play.Configuration;

/**
 * Created by jkidd on 4/15/15.
 */
public class ConfigurationService {
    private static final String PLUGIN_CONFIG = "plugins";
    private static final String PLUGIN_PATH = "path";

    public static String getPluginPath(){
        return Configuration.root().getConfig(PLUGIN_CONFIG).getString(PLUGIN_PATH);
    }
}
