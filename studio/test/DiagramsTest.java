import java.io.IOException;

import org.junit.*;

import play.mvc.Result;

import static play.test.Helpers.*;

import static org.fest.assertions.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test script to exercise Diagrams Controller.
 */
public class DiagramsTest extends TestBase{
    /**
     * Ensure that we can retrieve the test display by name.
     */
    @Test
    public void getDiagramTest() {
        running(fakeApplication(getSettings()), new Runnable() {
            public void run() {
                Result result = route(fakeRequest(GET, "/getDiagram/item/test1"));
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                System.out.println(contentAsString(result));
            }
        });
    }

    /**
     * Ensure that we can create a new diagram.
     */
    @Test
    public void saveDiagramTest() {
        running(fakeApplication(getSettings()), new Runnable() {
            public void run() {
                ObjectMapper mapper = new ObjectMapper();

                JsonNode json = null;

                try {
                    json = mapper.readValue("{\"name\":\"test101\",\"description\":\"description\",\"version\":1,\"owner\":\"test@test.com\",\"wires\":[{\"from_node\":\"LoadDB1\",\"from_connectorIndex\":5,\"to_node\":\"Merge1\",\"to_connectorIndex\":0},{\"from_node\":\"LoadDB2\",\"from_connectorIndex\":0,\"to_node\":\"Merge1\",\"to_connectorIndex\":0},{\"from_node\":\"Merge1\",\"from_connectorIndex\":0,\"to_node\":\"Columns1\",\"to_connectorIndex\":0},{\"from_node\":\"Columns1\",\"from_connectorIndex\":0,\"to_node\":\"WeightedAverage1\",\"to_connectorIndex\":0}],\"blocks\":[{\"name\":\"LoadDB1\",\"definition\":\"LoadDB\",\"state\":3,\"x\":100,\"y\":80,\"w\":200,\"inputConnectors\":[],\"outputConnectors\":[{\"type\":\"frame\",\"name\":\"out\",\"position\":\"BottomCenter\"}],\"parameters\":[{\"name\":\"Project\",\"value\":\"none\"},{\"name\":\"DataSet\",\"value\":\"Clean-97\"}]},{\"name\":\"LoadDB2\",\"definition\":\"LoadDB\",\"state\":0,\"x\":360,\"y\":80,\"w\":200,\"inputConnectors\":[],\"outputConnectors\":[{\"type\":\"frame\",\"name\":\"out\",\"position\":\"BottomCenter\"}],\"parameters\":[{\"name\":\"Project\",\"value\":\"none\"},{\"name\":\"DataSet\",\"value\":\"none\"}]},{\"name\":\"Merge1\",\"definition\":\"Merge\",\"state\":0,\"x\":230,\"y\":230,\"w\":200,\"inputConnectors\":[{\"type\":\"frame\",\"name\":\"in\",\"position\":\"TopCenter\"}],\"outputConnectors\":[{\"type\":\"frame\",\"name\":\"out\",\"position\":\"BottomCenter\"}],\"parameters\":[]},{\"name\":\"Columns1\",\"definition\":\"Columns\",\"state\":0,\"x\":230,\"y\":380,\"w\":200,\"inputConnectors\":[{\"type\":\"frame\",\"name\":\"in\",\"position\":\"TopCenter\"}],\"outputConnectors\":[{\"type\":\"frame\",\"name\":\"out\",\"position\":\"BottomCenter\"}],\"parameters\":[{\"name\":\"Columns\",\"value\":\"none\"}]},{\"name\":\"WeightedAverage1\",\"definition\":\"WeightedAverage\",\"state\":0,\"x\":230,\"y\":540,\"w\":200,\"inputConnectors\":[{\"type\":\"frame\",\"name\":\"in\",\"position\":\"TopCenter\"}],\"outputConnectors\":[{\"type\":\"frame\",\"name\":\"out\",\"position\":\"BottomCenter\"}],\"parameters\":[{\"name\":\"Weight\",\"value\":\"20\"}]}]}", JsonNode.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Result result = route(fakeRequest(POST, "/saveDiagram").withHeader("Content-Type", "application/json").withJsonBody(json));

                assertThat(status(result)).isEqualTo(OK);
            }
        });
    }

    /**
     * Ensure that we can get a list of available diagrams.
     */
    @Test
    public void getDiagramsTest() {
        running(fakeApplication(getSettings()), new Runnable() {
            public void run() {
                Result result = route(fakeRequest(GET, "/getDiagrams"));
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                System.out.println(contentAsString(result));
            }
        });
    }

    /**
     * Ensure we can delete a diagram from the database.
     */
    @Test
    public void removeDiagramTest() {
        running(fakeApplication(getSettings()), new Runnable() {
            public void run() {
                Result result = route(fakeRequest(GET, "/removeDiagram/test101"));
                assertThat(status(result)).isEqualTo(OK);
                System.out.println(contentAsString(result));
            }
        });
    }

    /**
     * Ensure we can get a blank diagram from the database.
     */
    @Test
    public void getBlankDiagramTest() {
        running(fakeApplication(getSettings()), new Runnable() {
            public void run() {
                Result result = route(fakeRequest(GET, "/getDiagram/item"));
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                System.out.println(contentAsString(result));
            }
        });
    }
}
