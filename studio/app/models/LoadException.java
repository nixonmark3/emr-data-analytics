package models;

public class LoadException extends RuntimeException {

    public LoadException(Throwable e) {
        super(e);
    }

    public LoadException(String m) {
        super(m);
    }
}

