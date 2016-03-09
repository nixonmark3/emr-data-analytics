package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.definition.ValueType;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Parameter;
import emr.analytics.models.diagram.Wire;
import emr.analytics.models.diagram.WireSummary;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class CompileHelper {

    public static void loadProperties(TargetEnvironments targetEnvironment, Properties properties){

        loadProperties(targetEnvironment.toString().toLowerCase(), properties);
    }

    public static void loadProperties(String name, Properties properties){

        String fileName = String.format("conf/%s.properties", name);
        try (InputStream stream = CompileHelper.class.getClassLoader().getResourceAsStream(fileName)){
            properties.load(stream);
        }
        catch(IOException | NullPointerException ex){
            throw new CompilerException(ex);
        }
    }

    public static String buildArguments(String[] arguments, Block block, List<WireSummary> wires){

        StringBuilder argumentBuilder = new StringBuilder();
        for(String argument : arguments){

            String[] properties = argument.split(":");
            if (properties.length == 2){

                switch(properties[0].toLowerCase()){
                    case "parameter":

                        Optional<Parameter> optionalParameter = block.getParameters().stream().filter(p -> p.getName().equals(properties[1])).findFirst();
                        if (optionalParameter.isPresent()){

                            Parameter parameter = optionalParameter.get();

                            String value;
                            if (parameter.getValueType() == ValueType.LIST) {
                                value = String.format("\"%s\"", parameter
                                        .getValue()
                                        .toString()
                                        .replace(", ", ",")
                                        .replace("[", "")
                                        .replace("]", "")
                                        .replace("*", "."));

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

                        Optional<WireSummary> optionalWire = wires.stream().filter(w -> w.getTo_connector().equals(properties[1])).findFirst();
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

    public static String createVariableName(String name){
        return String.format("_%s", name.replace("-", ""));
    }
}
