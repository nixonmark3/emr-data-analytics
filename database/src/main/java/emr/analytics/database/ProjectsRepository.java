package emr.analytics.database;

import com.mongodb.util.JSON;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import java.util.*;

public class ProjectsRepository {

    private static final String PROJECT_PREFIX = "bds-";
    private static final String DATA_SET_PROJECTION = "{_id: 0, name: 1}";
    private static final String DATA_SET_COLLECTION = "datasets";
    private static final String SORT_BY_NAME = "{name: 1}";
    private static final String COLLECTION_NOT_FOUND = "'%s' collection could not be found!";

    private MongoConnection connection;

    public ProjectsRepository(MongoConnection connection){
        this.connection = connection;
    }

    public List<String> all(){

        try {
            List<String> projects = new ArrayList<String>();

            List<String> databaseNames = this.connection.allDatabases();
            for (String databaseName : databaseNames) {

                if (databaseName.contains(PROJECT_PREFIX)) {
                    projects.add(databaseName.replace(PROJECT_PREFIX, ""));
                }
            }

            return projects;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
    }

    public String getDataSet(String projectName) {

        try {
            MongoCursor<DataSet> dataSets = null;
            projectName = PROJECT_PREFIX.concat(projectName);

            MongoCollection dataSetsCollection = connection.getCollection(projectName, DATA_SET_COLLECTION);
            if (dataSetsCollection != null)
                dataSets = dataSetsCollection.find().projection(DATA_SET_PROJECTION).sort(SORT_BY_NAME).as(DataSet.class);
            else
                throw new DatabaseException(String.format(COLLECTION_NOT_FOUND, DATA_SET_COLLECTION));

            return JSON.serialize(dataSets);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
    }
}
