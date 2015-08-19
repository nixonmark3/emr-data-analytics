package emr.analytics.service.consumers;

public class ConsumerException extends RuntimeException {

    public ConsumerException(Throwable e) {
        super(e);
    }

    public ConsumerException(String m) {
        super(m);
    }
}