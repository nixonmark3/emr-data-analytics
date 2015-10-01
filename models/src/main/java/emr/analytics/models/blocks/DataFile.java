package emr.analytics.models.blocks;

import java.io.Serializable;

public class DataFile implements Serializable {

    private String name;
    private int progress;
    private String path;

    public DataFile(){}

    public String getName(){ return this.name; }
    public void setName(String value) { this.name = value; }
    public int getProgress(){ return this.progress; }
    public void setProgress(int value) { this.progress = value; }
    public String getPath(){ return this.path; }
    public void setPath(String value) { this.path = value; }
}