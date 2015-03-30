package models.definition;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB schema for Parameter Options.
 */
public class ParameterOptions {
    public boolean isDependent() {
        return isDependent;
    }

    public void setIsDependent(boolean isDependent) {
        this.isDependent = isDependent;
    }

    public void setDependent(boolean isDependent) {
        this.isDependent = isDependent;
    }

    public List<String> getDependants() {
        return dependants;
    }

    public void setDependants(List<String> dependants) {
        this.dependants = dependants;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public List<String> getFieldOptions() {
        return fieldOptions;
    }

    public void setFieldOptions(List<String> fieldOptions) {
        this.fieldOptions = fieldOptions;
    }

    /**
     * Hidden Jackson constructor.
     */
    private ParameterOptions() {}

    /**
     * Private members.
     */
    private boolean isDependent;
    private List<String> dependants = new ArrayList<String>();
    private String inputType;
    private boolean isDynamic;
    private List<String> fieldOptions = new ArrayList<String>();
}
