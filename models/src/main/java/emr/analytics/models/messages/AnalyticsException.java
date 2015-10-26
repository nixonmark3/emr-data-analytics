package emr.analytics.models.messages;

public class AnalyticsException extends RuntimeException {

    public AnalyticsException(Throwable e) {
        super(e);
    }

    public AnalyticsException(String m) {
        super(m);
    }
}