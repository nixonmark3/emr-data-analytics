package models;

public class DiagramException extends RuntimeException {

    public DiagramException(Throwable e) {
        super(e);
    }

    public DiagramException(String m) {
        super(m);
    }
}

