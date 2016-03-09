package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Parameter;
import emr.analytics.models.diagram.Wire;
import emr.analytics.models.diagram.WireSummary;

import java.util.*;

public class PythonBlock extends SourceBlock {

    private static final String outputCollectionName = "output";

    public PythonBlock(Block block, List<WireSummary> wires){
        super(TargetEnvironments.PYTHON, block, wires);
    }

    @Override
    public String getCode(Set<UUID> compiled){

        StringBuilder builder = new StringBuilder();

        String variableName = String.format("_%s",
                this.block.getId().toString().replace("-", ""));

        List<Parameter> parameters = block.getParameters();

        // build a map of inputs
        Map<String, List<String>> inputs = new HashMap<String, List<String>>();
        for(WireSummary wire : this.wires){

            String connector = wire.getTo_connector();

            List<String> input;
            if (!inputs.containsKey(connector)){
                input = new ArrayList<String>();
                inputs.put(connector, input);
            }
            else{
                input = inputs.get(connector);
            }

            input.add(String.format("%s/%s", wire.getFromNodeName(), wire.getFrom_connector()));
        }

        // declare variable
        builder.append(String.format("%s = %s('%s', '%s')\n",
                variableName,
                block.getDefinition(),
                block.getName(),
                block.getId().toString()));

        // build parameter set
        builder.append(String.format("%s.parameters = {", variableName));
        for(int i = 0; i < parameters.size(); i++){
            if (i > 0)
                builder.append(", ");

            Parameter parameter = parameters.get(i);
            if (parameter.getValueType() == ValueType.LIST){

                builder.append(String.format("'%s': %s", parameter.getName(), toStringArray(parameter.getValue().toString())));
            }
            else if (parameter.getParameterType() == ParameterType.ENUMERATION
                || parameter.getParameterType() == ParameterType.STRING) {

                builder.append(String.format("'%s': '%s'", parameter.getName(), parameter.getValue().toString()));
            }
            else {

                builder.append(String.format("'%s': %s", parameter.getName(), parameter.getValue().toString()));
            }
        }
        builder.append("}\n");

        // build input set
        builder.append(String.format("%s.input_connectors = {", variableName));
        boolean initial = true;
        for(HashMap.Entry<String, List<String>> inputConnector : inputs.entrySet()){
            if (!initial)
                builder.append(", ");
            else
                initial = false;

            builder.append(String.format("'%s': [", inputConnector.getKey()));

            List<String> inputBlocks = inputConnector.getValue();
            for(int i = 0; i < inputBlocks.size(); i++){
                if (i > 0)
                    builder.append(", ");
                builder.append(String.format("'%s'", inputBlocks.get(i)));
            }

            builder.append("]");
        }
        builder.append("}\n");

        builder.append(String.format("%s.update(%s.execute(%s))\n\n",
                outputCollectionName,
                variableName,
                outputCollectionName));

        return builder.toString();
    }

    @Override
    public Package[] getPackages(){
        return new Package[] {
                new Package(Package.PackageType.IMPORT,
                        this.block.getDefinition(),
                        this.block.getDefinition())
        };
    }

    private String toStringArray(String value){

        if (value.equals("[]"))
            return value;

        String[] values = value.replaceAll("\\[|\\]", "").split(", ");
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = 0; i < values.length; i++){
            if (i > 0)
                builder.append(", ");
            builder.append(String.format("'%s'", values[i]));
        }
        builder.append("]");

        return builder.toString();
    }
}
