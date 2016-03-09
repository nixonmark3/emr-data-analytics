package emr.analytics.diagram.interpreter;

import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.WireSummary;

import java.util.ArrayList;
import java.util.List;

public abstract class StreamBlock extends SourceBlock {

    protected StreamConfig config;

    public StreamBlock(TargetEnvironments targetEnvironment, Block block){
        super(targetEnvironment, block, new ArrayList<WireSummary>());

        // parse Stream Configuration json
        ObjectMapper mapper = new ObjectMapper();
        this.config = mapper.convertValue(block.getParameters().get(0).getValue(), StreamConfig.class);
    }

    protected static class StreamConfig {

        private SourceTypes sourceType;
        private DataSource dataSource;

        public StreamConfig(){}

        public SourceTypes getSourceType(){ return this.sourceType; }
        public DataSource getDataSource() { return this.dataSource; }
    }
}
