package emr.analytics.diagram.interpreter;

public class CompilerException extends RuntimeException {

    public CompilerException(Throwable e) {
        super(e);
    }

    public CompilerException(String m) {
        super(m);
    }
}
