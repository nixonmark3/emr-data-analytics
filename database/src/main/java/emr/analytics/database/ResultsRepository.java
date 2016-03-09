package emr.analytics.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.PersistedOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ResultsRepository {

    private static final String collectionName = "results";

    private DBCollection collection;
    private GridFS gridFS;

    public ResultsRepository(MongoConnection connection){

        this.collection = connection.getMongoCollection(collectionName);
        this.gridFS = connection.getGridFS();
    }

    public String getAvailableResults(String blockName) {

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

        return JSON.serialize(availableResults);
    }

    public String getChartData(String blockName, List<String> features) {

        BasicDBList chartData = new BasicDBList();

        BasicDBList resultChartData = getChartData(blockName);
        if (resultChartData != null) {

            ArrayList<String> resultFeatures = (ArrayList<String>) resultChartData.get(0);
            chartData.add(features);
            chartData.add(resultChartData.get(1));

            for (String feature : features)
                chartData.add(resultChartData.get(resultFeatures.indexOf(feature) + 2));
        }

        return JSON.serialize(chartData);
    }

    public String getFeatureGridData(String blockName) {

        BasicDBList chartData = new BasicDBList();

        BasicDBList resultChartData = getChartData(blockName);
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

        return JSON.serialize(chartData);
    }

    /**
     * Retrieve the persisted outputs for the specified diagram.  Outputs persisted to the database
     * provide a mechanism to pass data from the offline diagram to the online diagram
     * @param diagram: Diagram
     * @return Map of persisted outputs
     */
    public Map<String, String> getPersistedOutputs(Diagram diagram){

        Map<String, String> persistedOutputs = new HashMap<>();
        for(PersistedOutput persistedOutput : diagram.getPersistedOutputs()){

            String output = this.getOutput(persistedOutput.getId(), persistedOutput.getName());
            String variableName = String.format("%s_%s", persistedOutput.getId(), persistedOutput.getName());

            persistedOutputs.put(variableName, output);
        }

        return persistedOutputs;
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

    public String getOutputResults(String blockName) {

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

        return JSON.serialize(outputResults);
    }

    public byte[] getPlot(String blockName) {

        try {
            byte[] image = null;
            GridFSDBFile gridFSDBFile = gridFS.findOne(blockName);
            if (gridFSDBFile != null) {
                image = toByteArray(gridFSDBFile);
            }

            return image;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
    }

    public String getStatistics(String blockName) {

        BasicDBList blockStatistics = new BasicDBList();
        BasicDBObject results = getResults(blockName);

        if (results != null)
            blockStatistics = (BasicDBList)results.get("Statistics");

        return JSON.serialize(blockStatistics);
    }

    // private methods

    private BasicDBList getChartData(String blockName) {

        BasicDBList chartData = new BasicDBList();

        try {

            String filename = blockName + "_data";
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

    private BasicDBObject getResults(String blockName) {

        try {

            BasicDBObject results = null;
            BasicDBObject query = new BasicDBObject("name", blockName);
            DBCursor cursor = this.collection.find(query);

            try {
                while (cursor.hasNext())
                    results = (BasicDBObject) cursor.next().get("Results");
            }
            finally {

                if (cursor != null)
                    cursor.close();
            }

            return results;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new DatabaseException(ex);
        }
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
}
