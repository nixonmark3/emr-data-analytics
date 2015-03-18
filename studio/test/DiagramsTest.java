import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import play.mvc.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

/**
 * Test script to exercise Diagrams Controller.
 */
public class DiagramsTest {
    /**
     * Ensure that we can retrieve the test display by name.
     */
    @Test
    public void getDiagramNamed() {
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("mongo.mongodb.host", "127.0.0.1");
        settings.put("mongo.database.name", "emr-data-analytics-studio");

        running(fakeApplication(settings), new Runnable() {
            public void run() {
                Result result = controllers.Diagrams.getDiagram("test");
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                //assertThat(contentAsString(result)).contains("Hello Play Framework");
                System.out.println(contentAsString(result));
            }
        });
    }
}
