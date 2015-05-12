package emr.analytics.service.spark;

public class ScalaCompilerException extends Exception{

    private static final String _message = "An exception occurred while compiling and running Scala code.";
    private String _additionalInfo;

    public ScalaCompilerException(String additionalInfo) {
        super(_message);
        this._additionalInfo = additionalInfo;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Addition Info: " + _additionalInfo;
    }
}
