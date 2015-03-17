package controllers;

import models.diagram.*;

import java.util.ArrayList;
import java.util.List;

public class TestDiagramGenerator {
    public Diagram generate() {
        Diagram testDiagram = new Diagram("test");
        testDiagram.setDescription("description");
        testDiagram.setOwner("test@test.com");

        List<Wire> wires = new ArrayList<Wire>();

        wires.add(new Wire("LoadDB1", 0, "Merge1", 0));
        wires.add(new Wire("LoadDB2", 0, "Merge1", 0));
        wires.add(new Wire("Merge1", 0, "Columns1", 0));
        wires.add(new Wire("Columns1", 0, "WeightedAverage1", 0));
        testDiagram.setWires(wires);

        List<Block> blocks = new ArrayList<Block>();
        blocks.add(createLoadDB1Block());
        blocks.add(createLoadDB2Block());
        blocks.add(createMerge1Block());
        blocks.add(createColumns1Block());
        blocks.add(createWeightedAverage1Block());
        testDiagram.setBlocks(blocks);

        return testDiagram;
    }

    private static Block createLoadDB1Block() {
        Block block = new Block("LoadDB1", "LoadDB");
        block.setState(0);
        block.setX(100);
        block.setY(80);
        block.setW(200);
        List<Connector> outputConnectors = new ArrayList<Connector>();
        outputConnectors.add(new Connector("out", "frame", "BottomCenter"));
        block.setOutputConnectors(outputConnectors);
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("Project", "FLINT_HILLS_20150211"));
        parameters.add(new Parameter("DataSet", "Clean-97"));
        block.setParameters(parameters);
        return block;
    }

    private static Block createLoadDB2Block() {
        Block block = new Block("LoadDB2", "LoadDB");
        block.setState(0);
        block.setX(360);
        block.setY(80);
        block.setW(200);
        List<Connector> outputConnectors = new ArrayList<Connector>();
        outputConnectors.add(new Connector("out", "frame", "BottomCenter"));
        block.setOutputConnectors(outputConnectors);
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("Project", "none"));
        parameters.add(new Parameter("DataSet", "none"));
        block.setParameters(parameters);
        return block;
    }

    private  static Block createMerge1Block() {
        Block block = new Block("Merge1", "Merge");
        block.setState(0);
        block.setX(230);
        block.setY(230);
        block.setW(200);
        List<Connector> inputConnectors = new ArrayList<Connector>();
        inputConnectors.add(new Connector("in", "frame", "TopCenter"));
        block.setInputConnectors(inputConnectors);
        List<Connector> outputConnectors = new ArrayList<Connector>();
        outputConnectors.add(new Connector("out", "frame", "BottomCenter"));
        block.setOutputConnectors(outputConnectors);
        return block;
    }

    private  static Block createColumns1Block() {
        Block block = new Block("Columns1", "Columns");
        block.setState(0);
        block.setX(230);
        block.setY(380);
        block.setW(200);
        List<Connector> inputConnectors = new ArrayList<Connector>();
        inputConnectors.add(new Connector("in", "frame", "TopCenter"));
        block.setInputConnectors(inputConnectors);
        List<Connector> outputConnectors = new ArrayList<Connector>();
        outputConnectors.add(new Connector("out", "frame", "BottomCenter"));
        block.setOutputConnectors(outputConnectors);
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("Columns", "none"));
        block.setParameters(parameters);
        return block;
    }

    private  static Block createWeightedAverage1Block() {
        Block block = new Block("WeightedAverage1", "WeightedAverage");
        block.setState(0);
        block.setX(230);
        block.setY(540);
        block.setW(200);
        List<Connector> inputConnectors = new ArrayList<Connector>();
        inputConnectors.add(new Connector("in", "frame", "TopCenter"));
        block.setInputConnectors(inputConnectors);
        List<Connector> outputConnectors = new ArrayList<Connector>();
        outputConnectors.add(new Connector("out", "frame", "BottomCenter"));
        block.setOutputConnectors(outputConnectors);
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("Weight", "20"));
        block.setParameters(parameters);
        return block;
    }
}
