package emr.analytics.models.definition;

public class Argument {

    private String name;
    private int type;
    private String value;

    private Argument() {}

    public Argument(String name, int type, String value){

        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName(){ return this.name; }

    public void setName(String name) {
        this.name = name;
    }

    public int getType(){ return this.type; }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue(){ return this.value; }

    public void setValue(String value) {
        this.value = value;
    }
}
