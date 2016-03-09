package emr.analytics.models.definition;

import java.util.ArrayList;
import java.util.List;

public class ParameterSource {

    private ParameterSourceTypes parameterSourceType;
    private String fileName;
    private String className;
    private List<Argument> arguments = new ArrayList<Argument>();

    private ParameterSource() {}

    public ParameterSource(ParameterSourceTypes parameterSourceType, String fileName, String className, List<Argument> arguments) {
        this.parameterSourceType = parameterSourceType;
        this.fileName = fileName;
        this.className = className;
        this.arguments = arguments;
    }

    public ParameterSourceTypes getParameterSourceType() {
        return this.parameterSourceType;
    }

    public void setParameterSourceType(ParameterSourceTypes parameterSourceType) { this.parameterSourceType = parameterSourceType; }

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

    public enum ParameterSourceTypes {
        JAR, PYTHONSCRIPT
    }
}
