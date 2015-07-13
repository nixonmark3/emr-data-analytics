package emr.analytics.service.jobs;

public class AnalyticsJobException extends Exception{

    private static final String _message = "An exception occurred while executing an analytics job.";
    private String _additionalInfo;

    public AnalyticsJobException(String additionalInfo) {
        super(_message);
        this._additionalInfo = additionalInfo;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Addition Info: " + _additionalInfo;
    }
}

