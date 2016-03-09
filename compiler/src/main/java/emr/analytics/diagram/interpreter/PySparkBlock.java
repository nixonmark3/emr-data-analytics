package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.*;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Parameter;
import emr.analytics.models.diagram.Wire;
import emr.analytics.models.diagram.WireSummary;

import java.util.*;
import java.util.stream.Collectors;

public class PySparkBlock extends SourceBlock {

    private ModeDefinition definition;

    public PySparkBlock(Block block, List<WireSummary> wires, Definition definition){
        super(TargetEnvironments.PYSPARK, block, wires);

        // todo: temporarily hardcode online mode, will be resolved when definition is flattened
        this.definition = definition.getOnlineDefinition();
        if (this.definition == null)
            this.definition = definition.getOfflineDefinition();
    }

    @Override
    public String getCode(Set<UUID> compiled){


        // create a variable name for this block based on the output connectors
        String variableName;
        if (block.getOutputConnectors().size() > 0)
            variableName = block.getOutputConnectors().stream()
                    .map(c -> String.format("%s_%s", CompileHelper.createVariableName(block.getId().toString()), c.getName()))
                    .collect(Collectors.joining(", "));
        else
            variableName = CompileHelper.createVariableName(block.getId().toString());

        // reference the block's signature
        Signature signature = definition.getSignature();
        // build the pyspark operations
        String objectName;
        String operations;
        switch(signature.getSignatureType()){

            case STRUCTURED:

                objectName = signature.getObjectName();
                operations = String.format(".%s(%s)",
                        signature.getMethodName(),
                        CompileHelper.buildArguments(signature.getArguments(), block, wires));

                break;
            case FUNCTIONAL:

                objectName = CompileHelper.buildArguments(new String[] { signature.getObjectName() },
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
                            CompileHelper.buildArguments(operation.getArguments(),
                                    block,
                                    wires)));
                }

                operations = operationsBuilder.toString();

                break;
            default:

                throw new RuntimeException("Invalid signature type.");
        }

        return String.format("%s = %s%s\n", variableName, objectName, operations);
    }

    @Override
    public Package[] getPackages(){

        List<Package> packages = new ArrayList<Package>();

        // reference the block's signature
        Signature signature = definition.getSignature();
        switch(signature.getSignatureType()){

            case FUNCTIONAL:

                Set<String> captured = new HashSet<>();
                for(Operation operation : signature.getOperations()){
                    String name = operation.getObjectName();
                    if(!captured.contains(name)){
                        packages.add(new Package(Package.PackageType.IMPORT,
                                name,
                                name));
                        captured.add(name);
                    }
                }

                break;

            case STRUCTURED:

                packages.add(new Package(Package.PackageType.IMPORT,
                        signature.getObjectName(),
                        signature.getObjectName()));

                break;
        }

        Package[] result = new Package[packages.size()];
        result = packages.toArray(result);
        return result;
    }
}
