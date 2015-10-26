package emr.analytics.service.messages;

import java.util.ArrayList;
import java.util.List;

public class DataFrameSchema {

    private List<DFSchemaField> fields;
    private String type;

    public DataFrameSchema(){
        fields = new ArrayList<DFSchemaField>();
    }

    public String getType() { return this.type; }

    public List<DFSchemaField> getFields() { return this.fields; }

    public static class DFSchemaField {

        private DFSchemaMetadata metadata;
        private String name;
        private boolean nullable;
        private String type;

        public DFSchemaField(){}

        public DFSchemaMetadata getMetadata() { return this.metadata; }

        public String getName() { return this.name; }

        public boolean getNullable() { return this.nullable; }

        public String getType() { return this.type; }
    }

    public static class DFSchemaMetadata {
        public DFSchemaMetadata() {}
    }
}
