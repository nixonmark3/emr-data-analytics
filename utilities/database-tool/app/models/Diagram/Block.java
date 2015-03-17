package models.diagram;

import scala.xml.PrettyPrinter;

import java.util.ArrayList;
import java.util.List;

public class Block {
    public Block(String name, String definition) {
        this.name = name;
        this.definition = definition;
        this.inputConnectors = new ArrayList<Connector>();
        this.outputConnectors = new ArrayList<Connector>();
        this.parameters = new ArrayList<Parameter>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public List<Connector> getInputConnectors() {
        return inputConnectors;
    }

    public void setInputConnectors(List<Connector> inputConnectors) {
        this.inputConnectors = inputConnectors;
    }

    public List<Connector> getOutputConnectors() {
        return outputConnectors;
    }

    public void setOutputConnectors(List<Connector> outputConnectors) {
        this.outputConnectors = outputConnectors;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    private String name;
    private String definition;
    private int state;
    private int x;
    private int y;
    private int w;
    private List<Connector> inputConnectors;
    private List<Connector> outputConnectors;
    private List<Parameter> parameters;
}
