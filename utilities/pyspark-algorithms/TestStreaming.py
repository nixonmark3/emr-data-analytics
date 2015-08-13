"""
 Spark streaming job that tests the new kafka direct stream feature

 Usage: StreamingTest.py
 To run locally, you must start kafka
 Example:
    $SPARK_HOME/bin/spark-submit --jars $SPARK_HOME/external/kafka-assembly/spark-streaming-kafka-assembly_2.10-1.4.1.jar TestStreaming.py
"""

from __future__ import print_function

from pyspark import SparkContext
from pyspark.streaming import StreamingContext

import StreamingSources
import Filters
import Transformations

sc = SparkContext(master="local[2]", appName="StreamingTest")
ssc = StreamingContext(sc, 1)

broker = "localhost:9092"
topic = "OPC"
featureNames = "IN7OUT1MODEL/TAG2.CV,IN7OUT1MODEL/TAG3.CV,IN7OUT1MODEL/TAG1.CV"

muX = [0.5, 0.5, 0.5]
sigmaX = [0.1, 0.1, 0.1]

muY = [0.4]
sigmaY = [0.2]

model = [2.1, 4.2, 3.8]

def post(rdd):
    results = rdd.collect()
    for result in results:
        for item in result:
            print(item)

stream = StreamingSources.kafkaStream(ssc, topic, broker)

predict = stream.map(lambda x: Filters.dictionaryToArray(x, featureNames))\
    .map(lambda x: Transformations.normalize(x, muX, sigmaX))\
    .map(lambda x: Transformations.dotProduct(x, model))\
    .map(lambda x: Transformations.deNormalize(x, muY, sigmaY))

predict.foreachRDD(lambda rdd: post(rdd))

ssc.start()
ssc.awaitTermination()