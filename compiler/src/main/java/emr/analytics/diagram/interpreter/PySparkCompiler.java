package emr.analytics.diagram.interpreter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.Parameter;
import emr.analytics.models.diagram.Wire;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class PySparkCompiler implements TargetCompiler {

    private final String template;
    private Diagram diagram;
    private HashMap<String, Definition> definitions;
    private HashMap<String, String> models;

    public PySparkCompiler(HashMap<String, Definition> definitions, Diagram diagram, HashMap<String, String> models){

        this.definitions = definitions;
        this.diagram = diagram;
        this.models = models;

        if (this.diagram.getMode() == Mode.ONLINE)
            this.template = "pyspark_streaming_driver.mustache";
        else
            this.template = "pyspark_driver.mustache";
    }

    public String compile(){

        // compile a list of blocks to execute
        PySparkBlocks blocks = new PySparkBlocks(diagram.getId(), diagram.getMode(), models);

        // Initialize queue of blocks to compile
        Queue<Block> queue = new LinkedList<Block>();
        for (Block block : diagram.getRoot()) {
            queue.add(block);
        }

        // Capture all configured blocks in order
        while (!queue.isEmpty()) {
            Block block = queue.remove();

            // Capture configured blocks and queue descending blocks
            if (block.isConfigured()) {

                Definition definition = definitions.get(block.getDefinition());
                blocks.add(definition, block, diagram.getLeadingWires(block.getId()));

                for (Block next : diagram.getNext(block.getId())) {
                    queue.add(next);
                }
            }
        }

        // compile configured blocks
        String source = "";
        if (!blocks.isEmpty()) {
            try {
                source = blocks.compile(template);
            }
            catch (IOException ex) {
                System.err.println(String.format("IOException: %s.", ex.toString()));
            }
        }

        return source;
    }

    public class PySparkBlocks {

        private final String terminatingDefinition = "Post";
        private Mode mode;

        public String diagramId;
        public HashSet<String> packages;
        public List<BlockOperation> blocks;
        public List<ModelVariable> models = new ArrayList<ModelVariable>();
        public String terminatingVariable;

        public PySparkBlocks(UUID diagramId, Mode mode, HashMap<String, String> models) {

            this.diagramId = diagramId.toString();
            this.mode = mode;

            models.forEach((key, value) -> {
                this.models.add(new ModelVariable(key, value));
            });

            this.packages = new HashSet<String>();
            this.blocks = new ArrayList<BlockOperation>();
        }

        public void add(Definition definition, Block block, List<Wire> wires) {

            ModeDefinition modeDefinition = definition.getModel(this.mode);

            if (definition.getName().equals(terminatingDefinition)){

                this.terminatingVariable = buildArguments(new String[] { "input:in" },
                        block,
                        wires);
            }

            Signature signature = modeDefinition.getSignature();
            if (signature == null){
                // todo: create exception
                return;
            }

            // capture new signature packages
            switch(signature.getSignatureType()){
                case FUNCTIONAL:
                    // confirm the package specified in each functional operation is captured
                    for(Operation operation : signature.getOperations()){
                        if(!packages.contains(operation.getObjectName()))
                            packages.add(operation.getObjectName());
                    }
                    break;

                case STRUCTURED:
                    // confirm the package specified
                    if(!packages.contains(signature.getObjectName()))
                        packages.add(signature.getObjectName());
                    break;
            }

            blocks.add(new BlockOperation(modeDefinition, block, wires));
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

        private String buildArguments(String[] arguments, Block block, List<Wire> wires){

            StringBuilder argumentBuilder = new StringBuilder();
            for(String argument : arguments){

                String[] properties = argument.split(":");
                if (properties.length == 2){

                    switch(properties[0].toLowerCase()){
                        case "parameter":

                            Optional<Parameter> optionalParameter = block.getParameters().stream().filter(p -> p.getName().equals(properties[1])).findFirst();

                            // todo: should parameter type be stored in the block ?

                            if (optionalParameter.isPresent()){

                                Parameter parameter = optionalParameter.get();
                                String parameterType = parameter.getType();

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
                                        createVariableName(wire.getFrom_node().toString()),
                                        wire.getFrom_connector()));
                            }
                            else{
                                argumentBuilder.append(argument);
                            }

                            break;
                        case "block":

                            // refers to persisted offline output
                            argumentBuilder.append(String.format("%s_%s",
                                    createVariableName(block.getId().toString()),
                                    properties[1]));

                            break;
                        case "lambda":

                            argumentBuilder.append(properties[1].toLowerCase());

                            break;
                    }
                }
                else{
                    argumentBuilder.append(argument);
                }

                argumentBuilder.append(", ");
            }

            return argumentBuilder.substring(0, argumentBuilder.length() - 2);
        }

        public class BlockOperation {

            public String variableName;
            public String objectName;
            public String operations;

            public BlockOperation(ModeDefinition modeDefinition, Block block, List<Wire> wires) {

                Signature signature = modeDefinition.getSignature();

                if (block.getOutputConnectors().size() > 0) {
                    this.variableName = block.getOutputConnectors().stream()
                            .map(c -> String.format("%s_%s", createVariableName(block.getId().toString()), c.getName()))
                            .collect(Collectors.joining(", "));
                }
                else{
                    this.variableName = createVariableName(block.getId().toString());
                }

                switch(signature.getSignatureType()){

                    case STRUCTURED:
                        buildStructuredOperation(signature, block, wires);
                        break;
                    case FUNCTIONAL:
                        buildFunctionalOperation(signature, block, wires);
                        break;
                }
            }

            private void buildStructuredOperation(Signature signature, Block block, List<Wire> wires){

                // set the structured operation's object name
                this.objectName = signature.getObjectName();

                this.operations = String.format(".%s(%s)",
                        signature.getMethodName(),
                        buildArguments(signature.getArguments(), block, wires));
            }

            private void buildFunctionalOperation(Signature signature, Block block, List<Wire> wires){

                // set the structured operation's object name
                this.objectName = buildArguments(new String[] { signature.getObjectName() },
                        block,
                        wires);

                StringBuilder operationsBuilder = new StringBuilder();
                for(Operation operation : signature.getOperations()){

                    operationsBuilder.append(".");

                    switch(operation.getOperationType()){
                        case FILTER:
                            operationsBuilder.append("filter(lambda x: ");
                            break;
                        case MAP:
                            operationsBuilder.append("map(lambda x: ");
                            break;
                    }

                    operationsBuilder.append(String.format("%s.%s(%s))",
                            operation.getObjectName(),
                            operation.getMethodName(),
                            buildArguments(operation.getArguments(),
                                    block,
                                    wires)));
                }

                this.operations = operationsBuilder.toString();
            }
        }

        public class ModelVariable {
            public String variableName;
            public String arguments;

            public ModelVariable(String variableName, String arguments){
                this.variableName = createVariableName(variableName);
                this.arguments = arguments;
            }
        }
    }
}
