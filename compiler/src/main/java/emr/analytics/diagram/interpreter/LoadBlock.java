package emr.analytics.diagram.interpreter;

import com.fasterxml.jackson.databind.ObjectMapper;
import emr.analytics.models.definition.TargetEnvironments;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.WireSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class LoadBlock extends SourceBlock {

    protected LoadConfig config;

    public LoadBlock(TargetEnvironments targetEnvironment, Block block){
        super(targetEnvironment, block, new ArrayList<WireSummary>());

        // parse Load Configuration json
        ObjectMapper mapper = new ObjectMapper();
        this.config = mapper.convertValue(block.getParameters().get(0).getValue(), LoadConfig.class);
    }

    //
    // Load request class
    //

    protected static class LoadConfig {
        private Source source;
        private Parse parse;
        private List<Transformation> transformations;

        public Source getSource() { return this.source; }

        public Parse getParse() { return this.parse; }

        public List<Transformation> getTransformations() { return this.transformations; }

        private LoadConfig() {
            this.transformations = new ArrayList<Transformation>();
        }
    }

    //
    // Parse classes
    //

    protected static class Parse {

        private UUID id;
        private ParseTypes parseType;
        private boolean isDirty;
        private DelimiterTypes delimiterType;
        private char delimiterChar;
        private String page;
        private String header;
        private String skip;
        private char quoteChar;
        private char commentChar;
        private String naValues;

        public Parse() {}

        public UUID getId() { return this.id; }

        public ParseTypes getParseType() { return this.parseType; }

        public boolean getIsDirty() { return this.isDirty; }

        public DelimiterTypes getDelimiterType() { return this.delimiterType; }

        public char getDelimiterChar() { return this.delimiterChar; }

        public String getPage() { return this.page; }

        public String getHeader() { return this.header; }

        public String getSkip() { return this.skip; }

        public char getQuoteChar() { return this.quoteChar; }

        public char getCommentChar() { return this.commentChar; }

        public String getNaValues() { return this.naValues; }
    }

    protected enum ParseTypes {
        SEPARATED_VALUES, SPREADSHEET
    }

    protected enum DelimiterTypes {
        COMMA, TAB, OTHER
    }

    //
    // Transformation class
    //

    protected static class Transformation {
        private UUID id;
        private TransformationTypes transformationType;
        private String source;

        public Transformation(){}

        public UUID getId() { return this.id; }

        public TransformationTypes getTransformationType() { return this.transformationType; }

        public String getSource() { return this.source; }
    }

    protected enum TransformationTypes {
        FILTER, MAP
    }
}
