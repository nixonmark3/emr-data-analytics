package models;

public class UploadResult {

    private String name;
    private String path;
    private String contentType;

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return this.path; }
    public void setPath(String path) { this.path = path;  }
    public String getContentType() { return this.contentType; }
    public void setContentType(String contentType) { this.contentType = contentType;  }
}
