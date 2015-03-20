import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all tests.
 */
public class TestBase {
    protected Map<String, String> getSettings() {
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("mongo.mongodb.host", "127.0.0.1");
        settings.put("mongo.database.name", "emr-data-analytics-studio");
        return settings;
    }
}
