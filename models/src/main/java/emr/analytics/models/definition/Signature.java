package emr.analytics.models.definition;

public class Signature {

    private String packageName;
    private String className;
    private String methodName;
    private String[] arguments;

    private Signature(){ }

    public Signature(String packageName, String className, String methodName){
        this(packageName, className, methodName, new String[] {});
    }

    public Signature(String packageName, String className, String methodName, String[] arguments){
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public String getPackageName(){
        return packageName;
    }

    public String getClassName(){
        return className;
    }

    public String getMethodName(){
        return methodName;
    }

    public String[] getArguments() {
        return arguments;
    }
}
