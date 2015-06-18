package emr.analytics.models.diagram;

import java.io.Serializable;

/**
 * MongoDB schema for Basic Diagram.
 */
public class BasicDiagram implements Serializable {

    private String name = "New Diagram";
    private String description = "";
    private String owner = "";
    private int height = 1000;
    private int width = 1000;

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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getHeight() {

        return height;
    }

    public void setHeight(int height) {

        this.height = height;
    }

    public int getWidth() {

        return width;
    }

    public void setWidth(int width) {

        this.width = width;
    }

    /**
     * Hidden Jackson constructor.
     */
    private BasicDiagram() {}
}