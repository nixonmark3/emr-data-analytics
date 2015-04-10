package models.definition;

import java.util.ArrayList;
import java.util.List;

public class ParameterOptions {
    private boolean dynamic = false;
    private List<String> dependencies = new ArrayList<String>();
    private List<String> fieldOptions = new ArrayList<String>();
    private String method = null;

    private ParameterOptions() {}

    public ParameterOptions(boolean dynamic, List<String> dependencies, List<String> fieldOptions, String method) {
        this.dynamic = dynamic;
        this.dependencies = dependencies;
        this.fieldOptions = fieldOptions;
        this.method = method;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getFieldOptions() {
        return fieldOptions;
    }

    public void setFieldOptions(List<String> fieldOptions) {
        this.fieldOptions = fieldOptions;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}