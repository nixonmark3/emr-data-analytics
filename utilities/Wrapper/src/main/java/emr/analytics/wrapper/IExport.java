package emr.analytics.wrapper;

import org.jongo.MongoCollection;

public interface IExport {

    public void export(MongoCollection definitions);
}
