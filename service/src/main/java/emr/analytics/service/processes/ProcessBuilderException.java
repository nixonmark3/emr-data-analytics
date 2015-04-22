package emr.analytics.service.processes;

public class ProcessBuilderException extends Exception{

    private static final String _message = "An exception occurred while creating a new Process.";
    private String _additionalInfo;

    public ProcessBuilderException(String additionalInfo) {
        super(_message);
        this._additionalInfo = additionalInfo;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Addition Info: " + _additionalInfo;
    }
}
