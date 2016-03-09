package emr.analytics.service.models;

public class Schema {

    private String name;
    private String type;

    public String getName(){ return this.name; }

    public String getType(){ return this.type; }

    public String toString(){ return String.format("%s: %s", this.name, this.type); }
}
