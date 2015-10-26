package emr.analytics.service;

public class SparkProcessException extends Exception{

    private static final String _message = "An exception occurred while creating a spark process.";
    private String _additionalInfo;

    public SparkProcessException(String additionalInfo) {
        super(_message);
        this._additionalInfo = additionalInfo;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Addition Info: " + _additionalInfo;
    }
}