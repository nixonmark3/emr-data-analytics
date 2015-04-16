package emr.analytics.models.definition;

import java.util.ArrayList;
import java.util.List;

public class ParameterSource {

    private String type;
    private String fileName;
    private String className;
    private List<Argument> arguments = new ArrayList<Argument>();

    private ParameterSource() {}

    public ParameterSource(String type, String fileName, String className, List<Argument> arguments) {
        this.type = type;
        this.fileName = fileName;
        this.className = className;
        this.arguments = arguments;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }
}
