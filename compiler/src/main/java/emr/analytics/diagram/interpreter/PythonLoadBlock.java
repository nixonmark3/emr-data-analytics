package emr.analytics.diagram.interpreter;

import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PythonLoadBlock extends LoadBlock {

    private static final String outputCollectionName = "output";

    public PythonLoadBlock(Block block){
        super(TargetEnvironments.PYTHON, block);
    }

    @Override
    public String getCode(Set<UUID> compiled){
        StringBuilder builder = new StringBuilder();

        Source source = this.config.getSource();
        Parse parse = this.config.getParse();
        List<Transformation> transformations = this.config.getTransformations();

        // validate the load configuration
        if (source.getDataSources().size() == 0)
            throw new CompilerException("Unable to load data, a file has not been specified.");

        // generate names for rdd and output based on block name
        String variableName = String.format("%s_%s",
                block.getName(),
                block.getOutputConnectors().get(0).getName())
                .replace(" ", "");
        String outputName = String.format("%s/%s",
                block.getName(),
                block.getOutputConnectors().get(0).getName());

        // report block execution start
        builder.append(String.format("print(\"%s,2\", flush=True)\n", block.getId().toString()));

        // check whether this is the first parse attempt
        boolean isInitial = false;
        if (!compiled.contains(parse.getId())) {
            isInitial = true;
            compiled.add(parse.getId());
        }

        // if necessary parse the data file
        if (isInitial || parse.getIsDirty() || true) {

            //
            switch (parse.getParseType()) {
                case SPREADSHEET:
                    // load excel file
                    builder.append(String.format("%s = pd.read_excel('%s')\n",
                            variableName,
                            source.getDataSources().get(0).getPath()));
                    break;
                case SEPARATED_VALUES:
                default:

                    // load delimited file
                    builder.append(String.format("%s = pd.read_csv('%s'",
                            variableName,
                            source.getDataSources().get(0).getPath()));

                    switch(parse.getDelimiterType()){
                        case TAB:
                            builder.append(",sep='\\t'");
                            break;
                    }

                    builder.append(")\n");

                    break;
            }

            // attempt to convert 'object' columns to datetime
            builder.append(String.format("for col in %s.columns:\n", variableName));
            builder.append(String.format("\tif %s[col].dtype == 'object':\n", variableName));
            builder.append("\t\ttry:\n");
            builder.append(String.format("\t\t\t%s[col] = pd.to_datetime(%s[col])\n", variableName, variableName));
            builder.append("\t\texcept ValueError:\n");
            builder.append("\t\t\tpass\n");
            builder.append("\n");
        }

        boolean executeAll = false;
        for(Transformation transformation : transformations){

            if (!compiled.contains(transformation.getId())) {
                executeAll = true;
                compiled.add(parse.getId());
            }

            if (executeAll){

                switch(transformation.getTransformationType()){
                    case FILTER:
                        // the configured source code should yield a boolean value
                        builder.append(String.format("%s = %s[%s.apply(%s, axis=1)]\n",
                                variableName,
                                variableName,
                                variableName,
                                transformation.getSource()));
                        break;
                    default:
                        throw new CompilerException(String.format("The Transformation Type specified, %s, is not supported.",
                                transformation.getTransformationType().toString()));
                }
            }
        }

        builder.append(String.format("%s[\"%s\"] = %s\n",
                outputCollectionName,
                outputName,
                variableName));

        builder.append(String.format("%s[\"%s/stats\"] = dataGateway.describe(\"%s\", %s)\n",
                outputCollectionName,
                outputName,
                outputName,
                variableName));

        // report block execution completion
        builder.append(String.format("print(\"%s,3\", flush=True)\n", block.getId().toString()));

        return builder.toString();
    }

    @Override
    public Package[] getPackages(){
        return new Package[] {};
    }
}
