package emr.analytics.models.definition;

public class Signature {

    private SignatureType signatureType;
    private String objectName;
    private String methodName;
    private String[] arguments;
    private Operation[] operations;

    private Signature(){ }

    public Signature(String objectName, String methodName, String[] arguments){

        // represents a structured signature
        this.signatureType = SignatureType.STRUCTURED;
        this.objectName = objectName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public Signature(String objectName, Operation[] operations){

        // represents 1-to-many functional calls
        this.signatureType = SignatureType.FUNCTIONAL;
        this.objectName = objectName;
        this.operations = operations;
    }

    public SignatureType getSignatureType() { return this.signatureType; }

    public String getObjectName(){ return this.objectName; }

    public String getMethodName(){
        return this.methodName;
    }

    public String[] getArguments() {
        return this.arguments;
    }

    public Operation[] getOperations() { return this.operations; }

    public enum SignatureType {
        STRUCTURED, FUNCTIONAL
    }
}
