package emr.analytics.models.diagram;

import java.io.Serializable;

/**
 * MongoDB schema for Basic Diagram.
 */
public class BasicDiagram implements Serializable {
    /**
     * Create a basic diagram with required properties only.
     * @return basic diagram
     */
    public static BasicDiagram CreateBasicDiagram() {
         return new BasicDiagram();
    }

    /**
     * Returns the name of this Diagram.
     * @return diagram name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Diagram.
     * @param name diagram name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this Diagram.
     * @return diagram description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this Diagram.
     * @param description diagram description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Hidden Jackson constructor.
     */
    private BasicDiagram() {}

    /**
     * Private members.
     */
    private String name = "New Diagram";
    private String description = "";
}