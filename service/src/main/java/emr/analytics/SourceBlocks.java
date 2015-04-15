package emr.analytics;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import emr.analytics.models.diagram.*;

public class SourceBlocks {
    public List<DefinitionImport> definitions;
    public List<SourceBlock> blocks;

    public SourceBlocks(Diagram diagram) {
        definitions = new ArrayList<DefinitionImport>();
        definitionNames = new HashSet<String>();
        blocks = new ArrayList<SourceBlock>();
    }

    public void add(Block block, List<Wire> wires) {
        if(!definitionNames.contains(block.getDefinition())) {
            String definitionName = block.getDefinition();
            definitions.add(new DefinitionImport(String.format("from %s import %s", definitionName, definitionName)));
            definitionNames.add(definitionName);
        }

        blocks.add(new SourceBlock(block, wires));
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
        public String definition;
        public String parameters;
        public String inputs;

        public SourceBlock(Block block, List<Wire> wires){
            name = block.getName();
            definition = block.getDefinition();

            // todo: string builders

            parameters = "";
            List<Parameter> params = block.getParameters();
            for(int i = 0; i < params.size(); i++){

                if (i > 0)
                    parameters += ", ";

                Parameter param = params.get(i);
                parameters += String.format("'%s': %s", param.getName(), param.getValue());
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
    }

    private HashSet<String> definitionNames;
}

