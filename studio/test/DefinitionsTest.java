import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import play.mvc.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

/**
 * Test script to exercise Definition retrieval.
 */
public class DefinitionsTest {
    /**
     * Ensure that we can get the definitions from the database.
     */
    @Test
    public void getDefinitions() {
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("mongo.mongodb.host", "127.0.0.1");
        settings.put("mongo.database.name", "emr-data-analytics-studio");

        running(fakeApplication(settings), new Runnable() {
            public void run() {
                Result result = controllers.Definitions.getDefinitions();

                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                //assertThat(contentAsString(result)).contains("Hello Play Framework");
                System.out.println(contentAsString(result));
            }
        });
    }

}
