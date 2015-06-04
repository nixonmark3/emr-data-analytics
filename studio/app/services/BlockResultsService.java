package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public static String getModel(String blockName){

        StringBuilder model = new StringBuilder();

        List<BasicDBObject> results = getOutputResults(blockName);
        Optional<BasicDBObject> result = results.stream().filter(r -> r.containsValue("scaled_coef")).findFirst();
        if (result.isPresent()){
            BasicDBObject data = (BasicDBObject)result.get().get("data");
            if (data != null) {

              data.forEach((k, v) -> {

                  model.append(v.toString());
                  model.append(",");
              });
            }
        }

        return (model.length() > 0) ? model.substring(0, model.length() - 1) : "";
    }

    public static BasicDBObject getFeatures(String blockName) {

        BasicDBObject features = new BasicDBObject();

        BasicDBObject results = BlockResultsService.getResults(blockName);

        if (results != null) {

            BasicDBList blockStatistics = (BasicDBList) results.get("Statistics");

            blockStatistics.forEach(featureStatisticsObj -> {

                BasicDBList tuple = (BasicDBList)featureStatisticsObj;

                if (tuple.size() == 2) {

                    features.put(tuple.get(0).toString(), tuple.get(1));
                }
            });
        }

        return features;
    }

    public static List<BasicDBObject> getStatistics(String blockName) {

        List<BasicDBObject> statistics = new ArrayList<BasicDBObject>();

        BasicDBObject results = BlockResultsService.getResults(blockName);

        if (results != null) {

            BasicDBList blockStatistics = (BasicDBList)results.get("Statistics");

            blockStatistics.forEach(featureStatisticsObj -> {

                BasicDBList tuple = (BasicDBList) featureStatisticsObj;

                if (tuple.size() == 2) {

                    BasicDBObject blockFeatureStatistics = new BasicDBObject();
                    blockFeatureStatistics.put("name", tuple.get(0));

                    BasicDBObject featureStatistics = (BasicDBObject) tuple.get(1);
                    featureStatistics.forEach((statistic, statisticValue) -> {

                        if (statistic.contains("25")) {
                            statistic = "twentyFive";
                        } else if (statistic.contains("50")) {
                            statistic = "fifty";
                        } else if (statistic.contains("75")) {
                            statistic = "seventyFive";
                        }

                        blockFeatureStatistics.put(statistic, statisticValue.toString());
                    });

                    statistics.add(blockFeatureStatistics);
                }
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

                    if (isListOfTuples(resultData)) {

                        resultData = convertTuplesToDictionary(resultData);

                        data.put("type", "dictOfValues");
                    }
                    else {
                        data.put("type", "listOfValues");
                    }
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

    private static boolean isListOfTuples(Object object) {

        boolean isTuple = false;

        BasicDBList list = (BasicDBList)object;

        if (list.size() > 0) {

            if (BasicDBList.class.isInstance(list.get(0))) {

                BasicDBList listItem = (BasicDBList)list.get(0);

                if (listItem.size() == 2) {

                    isTuple = true;
                }
            }
        }

        return isTuple;
    }

    private static BasicDBObject convertTuplesToDictionary(Object object) {

        BasicDBObject tupleDataAsDictionary = new BasicDBObject();

        BasicDBList list = (BasicDBList)object;

        if (object != null) {

            list.forEach(item -> {

                BasicDBList itemAsList = (BasicDBList)item;
                tupleDataAsDictionary.put((String)itemAsList.get(0), itemAsList.get(1));
            });
        }

        return tupleDataAsDictionary;
    }

    public static JsonNode getChartData(String blockName) {

        JsonNode chartData = null;

        try {

            MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

            DB db = mongoPlugin.getMongoDBInstance(mongoPlugin.getStudioDatabaseName());

            String filename = blockName + "_data";

            GridFS gridFS = new GridFS(db);

            GridFSDBFile gridFSDBFile = gridFS.findOne(filename);

            if (gridFSDBFile != null) {

                ByteArrayOutputStream os = new ByteArrayOutputStream();

                gridFSDBFile.writeTo(os);

                String out = new String(os.toByteArray(), "UTF-8");

                chartData = new ObjectMapper().readValue(out, JsonNode.class);
            }
        }
        catch (java.io.IOException exception) {

            exception.printStackTrace();
        }

        return chartData;
    }
}
