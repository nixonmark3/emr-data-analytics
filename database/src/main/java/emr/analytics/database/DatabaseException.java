package emr.analytics.database;

public class DatabaseException extends RuntimeException {

    public DatabaseException(Throwable e) {
        super(e);
    }

    public DatabaseException(String m) {
        super(m);
    }
}
