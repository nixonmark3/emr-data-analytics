package models.definition;

import org.bson.types.ObjectId;

import java.util.*;

public class Definition {
    private ObjectId _id = null;
    private int w = 200;
    private String description = null;
    private String name = null;
    private String category = null;
    private String friendlyName = null;
    private List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
    private List<ConnectorDefinition> inputConnectors = new ArrayList<ConnectorDefinition>();
    private List<ConnectorDefinition> outputConnectors  = new ArrayList<ConnectorDefinition>();

    private Definition() {}

    public Definition(String name, String friendlyName, String category) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.category = category;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public List<ParameterDefinition> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterDefinition> parameters) {
        this.parameters = parameters;
    }

    public List<ConnectorDefinition> getInputConnectors() {
        return inputConnectors;
    }

    public void setInputConnectors(List<ConnectorDefinition> inputConnectors) {
        this.inputConnectors = inputConnectors;
    }

    public List<ConnectorDefinition> getOutputConnectors() {
        return outputConnectors;
    }

    public void setOutputConnectors(List<ConnectorDefinition> outputConnectors) {
        this.outputConnectors = outputConnectors;
    }
}
