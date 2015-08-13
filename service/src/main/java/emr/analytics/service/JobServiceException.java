package emr.analytics.service;

public class JobServiceException extends RuntimeException {

    public JobServiceException(Throwable e) {
        super(e);
    }

    public JobServiceException(String m) {
        super(m);
    }
}
