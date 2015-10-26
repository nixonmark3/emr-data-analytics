package emr.analytics.service;

public class TaskServiceException extends RuntimeException {

    public TaskServiceException(Throwable e) {
        super(e);
    }

    public TaskServiceException(String m) {
        super(m);
    }
}
