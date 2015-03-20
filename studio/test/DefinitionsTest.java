import org.junit.*;

import play.mvc.*;

import static play.test.Helpers.*;

import static org.fest.assertions.Assertions.*;

/**
 * Test script to exercise Definition retrieval.
 */
public class DefinitionsTest extends TestBase {
    /**
     * Ensure that we can get the definitions from the database.
     */
    @Test
    public void getDefinitions() {
        running(fakeApplication(getSettings()), new Runnable() {
            public void run() {
                Result result = controllers.Definitions.getDefinitions();
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                System.out.println(contentAsString(result));
            }
        });
    }
}
