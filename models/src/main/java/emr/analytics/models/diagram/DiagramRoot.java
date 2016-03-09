package emr.analytics.models.diagram;

import emr.analytics.models.definition.Mode;
import emr.analytics.models.definition.TargetEnvironments;

import java.io.Serializable;

/**
 * MongoDB schema for Basic Diagram.
 */
public class DiagramRoot implements Serializable {

    private String name;
    private String description;
    private String owner;
    private Mode mode;
    private TargetEnvironments targetEnvironment;
    private String category;
    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    /**
     * Gets this diagram's mode
     * @return diagram's mode
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * Sets this diagram's mode
     * @param mode the mode to set this diagram to.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets this diagram's target environment
     * @return diagram's target environment
     */
    public TargetEnvironments getTargetEnvironment() {
        return this.targetEnvironment;
    }

    /**
     * Sets this diagram's target environment
     * @param targetEnvironment the target environment to set the diagram to.
     */
    public void setTargetEnvironment(TargetEnvironments targetEnvironment) {
        this.targetEnvironment = targetEnvironment;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Hidden Jackson constructor.
     */
    private DiagramRoot() {}
}