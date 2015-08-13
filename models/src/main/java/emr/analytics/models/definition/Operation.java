package emr.analytics.models.definition;

public class Operation {
    private OperationType operationType;
    private String objectName;
    private String methodName;
    private String[] arguments;

    public Operation(){}

    public Operation(OperationType operationType, String objectName, String methodName, String[] arguments){
        this.operationType = operationType;
        this.objectName = objectName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public OperationType getOperationType() { return this.operationType; }

    public String getObjectName(){
        return this.objectName;
    }

    public String getMethodName(){
        return this.methodName;
    }

    public String[] getArguments() {
        return this.arguments;
    }

    public enum OperationType {
        MAP, FILTER
    }
}
