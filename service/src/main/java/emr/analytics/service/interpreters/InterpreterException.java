package emr.analytics.service.interpreters;

public class InterpreterException extends RuntimeException {

    public InterpreterException(Throwable e) {
        super(e);
    }

    public InterpreterException(String m) {
        super(m);
    }
}
