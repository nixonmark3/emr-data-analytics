package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoadRequest implements Serializable {

    private final String sparkContextName = "sc";
    private final String sqlContextName = "sqlContext";
    private final String rddVariableName = "rdd";
    private final String variableName = "load_out";
    private final String pysparkCsvVariableName = "py_spark_csv";

    private UUID diagramId;
    private String diagramName;
    private Source source;
    private Parse parse;
    private List<Transformation> transformations;

    private LoadRequest() {
        transformations = new ArrayList<Transformation>();
    }

    public UUID getDiagramId() { return this.diagramId; }

    public String getDiagramName() { return this.diagramName; }

    public Source getSource() { return this.source; }

    public Parse getParse() { return this.parse; }

    public List<Transformation> getTransformations() { return this.transformations; }

    public String getCode(){

        if (source.getDataSources().size() == 0)
            throw new LoadException("Unable to load data, a file was not specified.");

        StringBuilder builder = new StringBuilder();

        if (parse.getParseType() == ParseTypes.SEPARATED_VALUES){

            builder.append(String.format("import PySparkCSV as %s", pysparkCsvVariableName));

            builder.append("\n");

            builder.append(String.format("%s = %s.textFile('file://%s')",
                    rddVariableName,
                    sparkContextName,
                    source.getDataSources().get(0).getPath()));

            builder.append("\n");

            builder.append(String.format("%s = %s.csvToDataFrame(%s, %s",
                    variableName,
                    pysparkCsvVariableName,
                    sqlContextName,
                    rddVariableName));

            builder.append(")");

//            builder.append(String.format("%s = %s.read.format('com.databricks.spark.csv')",
//                    variableName,
//                    sqlContextName));
//
//            // configure options
//            builder.append(".options(inferschema='true'");
//
//            if (parse.getHeader())
//                builder.append(", header='true'");
//
//            if (parse.getDelimiterType() == DelimiterTypes.TAB) {
//
//                builder.append(", delimiter='\t'");
//            }
//            else if (parse.getDelimiterType() == DelimiterTypes.OTHER && Character.isDefined(parse.getDelimiterChar())) {
//
//                builder.append(", delimiter='");
//                builder.append(parse.getDelimiterChar());
//                builder.append("'");
//            }
//
//            if (parse.getMissingValueMode() != MissingValueModes.PERMISSIVE){
//                builder.append(", mode='");
//                builder.append(parse.getMissingValueMode().toString());
//                builder.append("'");
//            }
//
//            if (Character.isDefined(parse.getQuoteChar()) && parse.getQuoteChar() != '"'){
//                builder.append(", quote='");
//                builder.append(parse.getQuoteChar());
//                builder.append("'");
//            }
//
//            if (Character.isDefined(parse.getCommentChar()) && parse.getCommentChar() != '#'){
//                builder.append(", comment='");
//                builder.append(parse.getCommentChar());
//                builder.append("'");
//            }
//
//            builder.append(String.format(").load('file://%s')",
//                    source.getDataSources().get(0).getPath()));
        }

        builder.append("\n");
        builder.append(String.format("dataGateway.describe(%s)", variableName));

//        System.out.println(builder.toString());

        return builder.toString();
    }

    //
    // Source classes
    //

    public static class Source implements Serializable {

        private SourceTypes sourceType;
        private List<DataSource> dataSources = new ArrayList<DataSource>();

        public Source(){}

        public SourceTypes getSourceType(){ return this.sourceType; }
        public List<DataSource> getDataSources() { return this.dataSources; }
    }

    public static class DataSource implements Serializable {
        private DataSourceTypes dataSourceType;
        private String name;
        private int progress;
        private String path;

        public DataSource(){}

        public DataSourceTypes getDataSourceType(){ return this.dataSourceType; }
        public String getName(){ return this.name; }
        public int getProgress(){ return this.progress; }
        public String getPath(){ return this.path; }
    }

    public enum SourceTypes {
        FILES
    }

    public enum DataSourceTypes {
        CONNECTION, FILE
    }

    //
    // Parse classes
    //

    public static class Parse implements Serializable {

        private ParseTypes parseType;
        private boolean header;
        private DelimiterTypes delimiterType;
        private char delimiterChar;
        private MissingValueModes missingValueMode;
        private char quoteChar;
        private char commentChar;

        public Parse() {}

        public ParseTypes getParseType() { return this.parseType; }

        public boolean getHeader() { return this.header; }

        public DelimiterTypes getDelimiterType() { return this.delimiterType; }

        public char getDelimiterChar() { return this.delimiterChar; }

        public MissingValueModes getMissingValueMode() { return this.missingValueMode; }

        public char getQuoteChar() { return this.quoteChar; }

        public char getCommentChar() { return this.commentChar; }
    }

    public enum ParseTypes {
        SEPARATED_VALUES
    }

    public enum DelimiterTypes {
        COMMA, TAB, OTHER
    }

    public enum MissingValueModes {
        PERMISSIVE, DROPMALFORMED, FASTFAIL
    }

    //
    // Transformation class
    //

    public static class Transformation implements Serializable {
        private TransformationTypes transformationType;
        private String statement;

        public Transformation(){}

        public TransformationTypes getTransformationType() { return this.transformationType; }

        public String getStatement() { return this.statement; }
    }

    public enum TransformationTypes {
        FILTER, MAP
    }
}
