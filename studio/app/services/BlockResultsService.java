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
import java.lang.reflect.Array;
import java.net.UnknownHostException;
import java.util.*;

import controllers.BlockResults;
import emr.analytics.models.diagram.Block;
import plugins.MongoDBPlugin;


public class BlockResultsService {

    public List<BasicDBObject> getAvailableResults(String blockName) {

        List<BasicDBObject> availableResults = new ArrayList<>();

        BasicDBObject results = getResults(blockName);

        if (results != null) {

            results.keySet().forEach(resultName -> {

                BasicDBObject availableResult = new BasicDBObject();
                availableResult.append("name", resultName);

                String resultType = "";
                if (resultName.equals("Statistics")) {

                    resultType = "stats";
                }
                else if (resultName.equals("Plot")) {

                    resultType = "plot";
                }
                else if (resultName.equals("Results")) {

                    resultType = "results";
                }

                if (!resultType.isEmpty()) {

                    availableResult.append("type", resultType);

                    availableResults.add(availableResult);
                }

            });
        }

        return availableResults;
    }

    public String getOutput(UUID id, String name){

        StringBuilder outputBuilder = new StringBuilder();

        BasicDBObject results = getResults(id.toString());
        if (results != null) {

            BasicDBList output = (BasicDBList) results.get(name);
            output.forEach(item -> {

                if (item instanceof Double) {

                    outputBuilder.append(item);
                    outputBuilder.append(",");
                } else {

                    BasicDBList tuple = (BasicDBList) item;
                    if (tuple.size() == 2) {

                        outputBuilder.append(tuple.get(1));
                        outputBuilder.append(",");
                    }
                }
            });
        }

        return (outputBuilder.length() > 0) ? outputBuilder.substring(0, outputBuilder.length() - 1) : "";
    }

    public BasicDBList getStatistics(String blockName) {

        BasicDBList blockStatistics = new BasicDBList();

        BasicDBObject results = getResults(blockName);

        if (results != null) {

            blockStatistics = (BasicDBList)results.get("Statistics");
        }

        return blockStatistics;
    }

    public byte[] getPlot(String blockName) {

        byte[] image = null;

        try {

            MongoDBPlugin mongoPlugin = MongoDBPlugin.getMongoDbPlugin();

            DB db = mongoPlugin.getMongoDBInstance(mongoPlugin.getStudioDatabaseName());

            GridFS gridFS = new GridFS(db);

            GridFSDBFile gridFSDBFile = gridFS.findOne(blockName);

            if (gridFSDBFile != null) {

                image = toByteArray(gridFSDBFile);
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

    public List<BasicDBObject> getOutputResults(String blockName) {

        List<BasicDBObject> outputResults = new ArrayList<BasicDBObject>();

        BasicDBObject results = getResults(blockName);

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

    private BasicDBObject getResults(String blockName) {

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

    private byte[] toByteArray(GridFSDBFile file) throws IOException {

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

    private boolean isListOfTuples(Object object) {

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

    private BasicDBObject convertTuplesToDictionary(Object object) {

        BasicDBObject tupleDataAsDictionary = new BasicDBObject();

        BasicDBList list = (BasicDBList)object;

        if (object != null) {

            list.forEach(item -> {

                BasicDBList itemAsList = (BasicDBList) item;
                tupleDataAsDictionary.put((String) itemAsList.get(0), itemAsList.get(1));
            });
        }

        return tupleDataAsDictionary;
    }

    public BasicDBList getChartData(String blockName, List<String> features) {

        BasicDBList chartData = new BasicDBList();

        BasicDBList resultChartData = _getChartData(blockName);

        if (resultChartData != null) {

            ArrayList<String> resultFeatures = (ArrayList<String>) resultChartData.get(0);

            chartData.add(features);
            chartData.add(resultChartData.get(1));

            for (String feature : features) {

                chartData.add(resultChartData.get(resultFeatures.indexOf(feature) + 2));
            }
        }

        return chartData;
    }

    public BasicDBList getFeatureGridData(String blockName) {

        BasicDBList chartData = new BasicDBList();

        BasicDBList resultChartData = _getChartData(blockName);

        if (resultChartData != null) {

            for (int i = 0; i < resultChartData.size(); i++) {

                if (i == 0) {

                    chartData.add(resultChartData.get(i));
                    continue;
                }

                ArrayList<Object> column = (ArrayList<Object>) resultChartData.get(i);

                if (column.size() > 100) {

                    List<Object> reducedColumn = column.subList(0, 100);

                    chartData.add(reducedColumn);
                }
                else {

                    chartData.add(resultChartData.get(i));
                }
            }
        }

        return chartData;
    }

    private BasicDBList _getChartData(String blockName) {

        BasicDBList chartData = new BasicDBList();

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

                chartData = new ObjectMapper().readValue(out, BasicDBList.class);
            }
        }
        catch (java.io.IOException exception) {

            exception.printStackTrace();
        }

        return chartData;
    }
}
