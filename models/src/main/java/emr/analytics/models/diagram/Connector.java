package emr.analytics.models.diagram;

import emr.analytics.models.definition.ConnectorDefinition;

import java.io.Serializable;

/**
 * Jackson schema for Diagram Connector.
 */
public class Connector implements Serializable {
    /**
     * Returns the name of this Connector.
     * @return connector name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Connector.
     * @param name connector name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type (data direction) of this Connector.
     * @return data direction of connector
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type (data direction) of this Connector.
     * @param type data direction of connector
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the position of this Connector as it will appear on it parent Block.
     * @return position of connector
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the position of this Connector as it will appear on it parent Block.
     * @param position position of connector
     */
    public void setPosition(String position) {
        this.position = position;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getPersisted() {
        return this.persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Connector() {}

    /**
     * Private members.
     */
    private String type;
    private String name;
    private String position;
    private boolean visible;
    private boolean persisted;

    public Connector(ConnectorDefinition connectorDefinition){
        this(connectorDefinition.getName(),
                connectorDefinition.getType(),
                connectorDefinition.getVisible(),
                connectorDefinition.getPersisted());
    }

    public Connector(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Connector(String name, String type, boolean visible, boolean persisted){
        this(name, type);

        this.visible = visible;
        this.persisted = persisted;

        // todo: position ?
        this.position = "";
    }
}