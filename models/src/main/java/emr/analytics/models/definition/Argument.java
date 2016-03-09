package emr.analytics.models.definition;

public class Argument {

    private String name;
    private String path;
    private String value;

    private Argument() {}

    public Argument(String name, String path){

        this.name = name;
        this.path = path;
    }

    public String getName(){ return this.name; }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath(){ return this.path; }

    public void setPath(String path) { this.path = path; }

    public String getValue(){ return this.value; }

    public void setValue(String value) {
        this.value = value;
    }
}
