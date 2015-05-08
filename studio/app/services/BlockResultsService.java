package services;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.*;

import java.io.InputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import plugins.MongoDBPlugin;


public class BlockResultsService {

    public static List<String> getAvailableResults(String blockName) {

        List<String> availableResults = new ArrayList<String>();

        BasicDBObject results = BlockResultsService.getResults(blockName);

        if (results != null) {

            results.keySet().forEach(s -> availableResults.add(s));
        }

        return availableResults;
    }

    public static List<BasicDBObject> getStatistics(String blockName) {

        List<BasicDBObject> statistics = new ArrayList<BasicDBObject>();

        BasicDBObject results = BlockResultsService.getResults(blockName);

        if (results != null) {

            BasicDBObject blockStatistics = (BasicDBObject)results.get("Statistics");

            blockStatistics.forEach((featureName, featureStatisticsObj) -> {

                BasicDBObject blockFeatureStatistics = new BasicDBObject();
                blockFeatureStatistics.put("name", featureName);

                BasicDBObject featureStatistics = (BasicDBObject) featureStatisticsObj;
                featureStatistics.forEach((statistic, statisticValue) -> {

                    if (statistic.contains("25")) {
                        statistic = "twentyFive";
                    }
                    else if (statistic.contains("50")) {
                        statistic = "fifty";
                    }
                    else if (statistic.contains("75")) {
                        statistic = "seventyFive";
                    }

                    blockFeatureStatistics.put(statistic, statisticValue.toString());
                });

                statistics.add(blockFeatureStatistics);
            });
        }

        return statistics;
    }

    public static byte[] getPlot(String blockName) {

        byte[] image = null;

        try {
            MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

            DB db = mongoPlugin.getMongoDBInstance(mongoPlugin.getStudioDatabaseName());

            GridFS gridFS = new GridFS(db);

            GridFSDBFile gridFSDBFile = gridFS.findOne(blockName);

            if (gridFSDBFile != null) {

                image =  BlockResultsService.toByteArray(gridFSDBFile);
            }
        }
        catch (UnknownHostException unknownHostException) {

            unknownHostException.printStackTrace();
        }
        catch (IOException ioException) {

            ioException.printStackTrace();
        }

        return image;
    }

    public static List<BasicDBObject> getOutputResults(String blockName) {

        List<BasicDBObject> outputResults = new ArrayList<BasicDBObject>();

        BasicDBObject results = BlockResultsService.getResults(blockName);

        if (results != null) {

            BasicDBObject blockResults = (BasicDBObject)results.get("Results");

            blockResults.forEach((resultName, resultData) -> {

                BasicDBObject data = new BasicDBObject();
                data.put("name", resultName);

                if (BasicDBObject.class.isInstance(resultData)) {
                    data.put("type", "dictOfValues");
                }
                else if (BasicDBList.class.isInstance(resultData)) {
                    data.put("type", "listOfValues");
                }
                else {
                    data.put("type", "singleValue");
                }

                data.put("data", resultData);

                outputResults.add(data);
            });
        }

        return outputResults;
    }


    private static BasicDBObject getResults(String blockName) {

        BasicDBObject results = null;

        try {

            BasicDBObject query = new BasicDBObject("name", blockName);

            MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

            DB db = mongoPlugin.getMongoDBInstance(mongoPlugin.getStudioDatabaseName());

            DBCollection resultsCollection = db.getCollection("results");

            if (resultsCollection != null) {

                DBCursor cursor = resultsCollection.find(query);

                try {
                    while (cursor.hasNext()) {
                        results = (BasicDBObject) cursor.next().get("Results");
                    }
                }
                finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }

        return results;
    }

    private static byte[] toByteArray(GridFSDBFile file) throws IOException {

        InputStream is = file.getInputStream();

        int len = (int)file.getLength();
        byte[] b = new byte[len];

        int pos = 0;

        while (len > 0) {

            int read=is.read(b,pos,len);
            pos+=read;
            len-=read;
        }

        return b;
    }
}
