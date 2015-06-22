package emr.analytics.service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.*;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import com.mongodb.MongoClient;

import emr.analytics.models.definition.DataType;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.*;

import org.jongo.Jongo;

public class SourceBlocks {
    public List<DefinitionImport> definitions;
    public List<SourceBlock> blocks;

    private HashSet<String> definitionNames;
    private HashMap<String, Definition> definitionsMap = null;

    public SourceBlocks() {
        definitions = new ArrayList<DefinitionImport>();
        definitionNames = new HashSet<String>();
        blocks = new ArrayList<SourceBlock>();
        definitionsMap = this.getDefinitions();
    }

    public void add(Block block, List<Wire> wires) {
        if(!definitionNames.contains(block.getDefinition())) {
            String definitionName = block.getDefinition();
            definitions.add(new DefinitionImport(String.format("from %s import %s", definitionName, definitionName)));
            definitionNames.add(definitionName);
        }

        Definition definition = definitionsMap.get(block.getDefinition());

        blocks.add(new SourceBlock(definition, block, wires));
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

    public class DefinitionImport {
        public String definitionImport;

        public DefinitionImport(String definitionImport) {
            this.definitionImport = definitionImport;
        }
    }

    public class SourceBlock {
        public String name;
        public UUID id;
        public String variableName;
        public String definitionName;
        public String parameters;
        public String inputs;

        public SourceBlock(Definition definition, Block block, List<Wire> wires) {
            name = block.getName();
            id = block.getId();
            variableName = createVariableNameFromUniqueName(block.getId().toString());
            definitionName = block.getDefinition();

            // todo: string builders

            parameters = "";
            List<Parameter> params = block.getParameters();
            for(int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    parameters += ", ";
                }

                Parameter param = params.get(i);

                String parameterType = param.getType();

                if (parameterType != null) {

                    if (parameterType.equals(DataType.LIST.toString())
                            || parameterType.equals(DataType.STRING.toString())) {

                        parameters += String.format("'%s': '%s'", param.getName(), param.getValue());
                    }
                    else if (parameterType.equals(DataType.MULTI_SELECT_LIST.toString())) {

                        parameters += String.format("'%s': %s", param.getName(), adaptValueToStringArray(param.getValue()));

                    } else {

                        parameters += String.format("'%s': %s", param.getName(), param.getValue());
                    }
                }
            }

            HashMap<String, List<String>> inputVariables = new HashMap<String, List<String>>();
            for(Wire wire : wires){

                List<String> input;
                if(!inputVariables.containsKey(wire.getTo_connector())) {
                    input = new ArrayList<String>();
                    inputVariables.put(wire.getTo_connector(), input);
                }
                else {
                    input = inputVariables.get(wire.getTo_connector());
                }

                input.add(String.format("%s/%s", wire.getFrom_node(), wire.getFrom_connector()));
            }

            // todo: string builders

            inputs = "";
            for(HashMap.Entry<String, List<String>> variables : inputVariables.entrySet()){
                if (!inputs.isEmpty())
                    inputs += ", ";

                inputs += String.format("'%s': [", variables.getKey());

                List<String> leadingBlocks = variables.getValue();
                for(int i = 0; i < leadingBlocks.size(); i++){
                    if (i > 0)
                        inputs += ", ";

                    inputs += String.format("'%s'", leadingBlocks.get(i));
                }
                inputs += "]";
            }
        }

        private String adaptValueToStringArray(Object value) {

            if (value.toString().equals("[]")) {
                return value.toString();
            }

            String[] items = value.toString().replaceAll("\\[|\\]", "").split(", ");

            StringBuilder sb = new StringBuilder();
            sb.append("[");

            for (int idx = 0; idx < items.length; idx++) {

                if (idx > 0) {
                    sb.append(", ");
                }

                sb.append(String.format("'%s'", items[idx]));
            }

            sb.append("]");

            return sb.toString();
        }
    }

    /**
     * Returns all block definitions that we currently have in a (key=DefinitionName, Value=DefinitionInstance) pair
     * @return Hash Map of definition name to definition instance
     */
    private HashMap<String, Definition> getDefinitions() {

        HashMap<String, Definition> definitionMap = new HashMap<String, Definition>();

        MongoClient mongoClient = null;

        try {
            mongoClient = new MongoClient();

            Jongo db = new Jongo(mongoClient.getDB("emr-data-analytics-studio"));

            for (Definition definition : db.getCollection("definitions").find().as(Definition.class)) {
                definitionMap.put(definition.getName(), definition);
            }
        }
        catch (UnknownHostException excpetion) {
            excpetion.printStackTrace();
        }
        finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

        return definitionMap;
    }

    private String createVariableNameFromUniqueName(String uniqueName) {
        StringBuilder variableName = new StringBuilder();
        variableName.append('_');
        variableName.append(uniqueName.replaceAll("-", "_"));
        return variableName.toString();
    }
}

