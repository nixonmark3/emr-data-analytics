package emr.analytics.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import emr.analytics.models.definition.DataType;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.Signature;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Parameter;
import emr.analytics.models.diagram.Wire;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class StreamingSourceBlocks {

    private String _streamingContextName = "pollingstream";

    public HashSet<String> packageNames;
    public StreamingSourceBlock streamingContext;
    public List<StreamingSourceBlock> blocks;
    public List<StreamingModel> models = new ArrayList<StreamingModel>();

    public StreamingSourceBlocks(HashMap<String, String> models) {

        models.forEach((k, v) -> {
            this.models.add(new StreamingModel(k, v));
        });

        packageNames = new HashSet<String>();
        this.streamingContext = null;
        blocks = new ArrayList<StreamingSourceBlock>();
    }

    public void add(Definition definition, Block block, List<Wire> wires) {

        Signature signature = definition.getSignature();
        if (signature == null){
            // todo: create exception
            return;
        }

        // check whether package has been captured
        if(!packageNames.contains(signature.getPackageName()))
            packageNames.add(signature.getPackageName());

        if (definition.getName().toLowerCase().equals(_streamingContextName)){
            streamingContext = new StreamingSourceBlock(definition, block, wires);
        }
        else{
            blocks.add(new StreamingSourceBlock(definition, block, wires));
        }
    }

    public boolean isEmpty() {
        return this.blocks.isEmpty();
    }

    public String compile(String template) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(template);

        StringWriter writer = new StringWriter();
        mustache.execute(writer, this);
        return writer.toString();
    }

    public String createVariableName(String name){

        return String.format("_%s", name.replace("-", ""));
    }

    public class StreamingSourceBlock {
        public String variableName;
        public String className;
        public String methodName;
        public String arguments;

        public StreamingSourceBlock(Definition definition, Block block, List<Wire> wires) {

            Signature signature = definition.getSignature();

            if (block.getOutputConnectors().size() > 0) {
                this.variableName = block.getOutputConnectors().stream()
                        .map(c -> String.format("%s_%s", createVariableName(block.getUniqueName()), c.getName()))
                        .collect(Collectors.joining(", "));
            }
            else{
                this.variableName = createVariableName(block.getUniqueName());
            }

            this.className = signature.getClassName();

            this.methodName = signature.getMethodName();

            StringBuilder argumentBuilder = new StringBuilder();
            for(String argument : signature.getArguments()){

                String[] properties = argument.split(":");
                if (properties.length == 2){

                    switch(properties[0].toLowerCase()){
                        case "parameter":

                            Optional<Parameter> optionalParameter = block.getParameters().stream().filter(p -> p.getName().equals(properties[1])).findFirst();

                            // todo: should parameter type be stored in the block ?

                            if (optionalParameter.isPresent()){

                                Parameter parameter = optionalParameter.get();
                                String parameterType = definition.getTypeOfParameterDefinition(parameter.getName());

                                String value;
                                if (parameterType.equals(DataType.MULTI_SELECT_LIST.toString())) {
                                    value = String.format("\"%s\"", parameter
                                            .getValue()
                                            .toString()
                                            .replace(", ", ",")
                                            .replace("[", "")
                                            .replace("]", "")
                                            .replace("*", "."));    //

                                    // todo: temporarily hard code test tags
                                    // value = "\"PICK_P101/PV.CV,PICK_T101/PV.CV,PICK_F101/PV.CV\"";

                                }
                                else {
                                    value = String.format("\"%s\"", parameter.getValue().toString());
                                }

                                argumentBuilder.append(value);
                            }
                            else{
                                argumentBuilder.append(argument);
                            }

                            break;
                        case "input":

                            Optional<Wire> optionalWire = wires.stream().filter(w -> w.getTo_connector().equals(properties[1])).findFirst();
                            if (optionalWire.isPresent()){
                                Wire wire = optionalWire.get();
                                argumentBuilder.append(String.format("%s_%s",
                                    createVariableName(wire.getFrom_node()),
                                    wire.getFrom_connector()));
                            }
                            else{
                                argumentBuilder.append(argument);
                            }

                            break;
                        case "block":


                            switch(properties[1]){
                                case "model":

                                    if(block.hasOfflineComplement()){
                                        argumentBuilder.append(createVariableName(block.getOfflineComplement()));
                                    }
                                    else{
                                        argumentBuilder.append(argument);
                                    }

                                    break;
                            }

                            break;
                    }
                }
                else{
                    argumentBuilder.append(argument);
                }

                argumentBuilder.append(", ");
            }

            this.arguments = argumentBuilder.substring(0, argumentBuilder.length() - 2);
        }
    }

    public class StreamingModel {
        public String variableName;
        public String arguments;

        public StreamingModel(String variableName, String arguments){
            this.variableName = createVariableName(variableName);
            this.arguments = arguments;
        }
    }
}


