package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Query {

    private String query_name;
    private String docType;
    private String version;
    private int sampleRateSecs;

    private List<DateTimeRange> timeSelector;
    private List<Tag> columns;

    public Query(){
        this.query_name = "query";
        this.docType = "json";
        this.version = "1.0";
        this.sampleRateSecs = 60;
        this.timeSelector = new ArrayList<DateTimeRange>();
        this.columns = new ArrayList<Tag>();
    }

    public String getQuery_name(){ return this.query_name; }

    public String getDocType() { return this.docType; }

    public String getVersion() { return this.version; }

    public int getSampleRateSecs() { return this.sampleRateSecs; }

    public List<DateTimeRange> getTimeSelector() { return this.timeSelector; }

    public List<Tag> getColumns() { return this.columns; }

    public static class DateTimeRange {
        private Date startTime;
        private Date endTime;

        public DateTimeRange(){}

        public Date getStartTime() { return this.startTime; }

        public Date getEndTime() { return this.endTime; }
    }

    public static class Tag {
        private String tag;
        private String alias;
        private String dataType;
        private String renderType;
        private String format;

        public Tag() {}

        public String getTag() { return this.tag; }

        public String getAlias() { return this.alias; }

        public String getDataType() { return this.dataType; }

        public String getRenderType() { return this.renderType; }

        public String getFormat() { return this.format; }
    }
}
