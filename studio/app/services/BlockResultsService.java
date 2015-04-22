package services;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class BlockResultsService {

    public static List<String> getAvailableResults(String blockName) {

        List<String> availableResults = new ArrayList<String>();

        BlockResultsService.getResults(blockName).keySet().forEach(s -> availableResults.add(s));

        return availableResults;
    }

    public static List<BasicDBObject> getStatistics(String blockName) {

        List<BasicDBObject> statistics = new ArrayList<BasicDBObject>();

        BasicDBObject blockStatistics = (BasicDBObject)BlockResultsService.getResults(blockName).get("Statistics");

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

                blockFeatureStatistics.put(statistic, statisticValue);
            });

            statistics.add(blockFeatureStatistics);
        });

        return statistics;
    }

    public static List<String> getPlot(String blockName) {

        return new ArrayList<String>();
    }

    public static List<String> getOutputResults(String blockName) {

        return new ArrayList<String>();
    }


    private static BasicDBObject getResults(String blockName) {

        BasicDBObject results = null;

        try {

            BasicDBObject query = new BasicDBObject("name", blockName);
            DBCollection resultsCollection = new MongoClient().getDB("emr-data-analytics-studio").getCollection("results");
            DBCursor cursor = resultsCollection.find(query);

            try {

                while(cursor.hasNext()) {

                    results = (BasicDBObject)cursor.next().get("Results");
                }
            }
            finally {

                cursor.close();
            }
        }
        catch (UnknownHostException exception) {

            exception.printStackTrace();
        }

        return results;

    }
}
