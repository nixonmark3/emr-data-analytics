package emr.analytics.service.sources;

public class SourceException extends RuntimeException {

    public SourceException(Throwable e) {
        super(e);
    }

    public SourceException(String m) {
        super(m);
    }
}