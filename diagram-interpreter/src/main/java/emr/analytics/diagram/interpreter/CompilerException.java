package emr.analytics.diagram.interpreter;

public class CompilerException extends Exception{

    private static final String _message = "An exception occurred while compiling the specified diagram.";
    private String _additionalInfo;

    public CompilerException(String additionalInfo) {
        super(_message);
        this._additionalInfo = additionalInfo;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Addition Info: " + _additionalInfo;
    }
}
