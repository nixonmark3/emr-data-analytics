
'''
$SPARK_HOME/bin/spark-submit --jars /usr/local/spark/external/kafka-assembly/spark-streaming-kafka-assembly_2.10-1.6.0.jar TestQueueStream.py
'''

import StreamingSources
import Filters
import Transformations
from kafka import KeyedProducer, KafkaClient

from pyspark import SparkContext
from pyspark.streaming import StreamingContext

sc = SparkContext(appName="QueueStreamExperiment")
ssc = StreamingContext(sc, 1)

kafka = KafkaClient("127.0.0.1:9092")
producer = KeyedProducer(kafka)

def output(rdd):
    results = rdd.collect()
    for result in results:
        for item in result:
            producer.send_messages("ONLINE", "fd17841c-eba5-4d8c-8188-44dbd2a04a15".encode("UTF-8"), str(item).encode("UTF-8"))

_30875491d5aca995fc6528dbd253c5b5_model = [-0.009025109646838444,0.17103733677273814,0.29682962971806404,0.4287190756249719,0.443719180508577,0.6783851002640046,0.6308060426124875]
_30875491d5aca995fc6528dbd253c5b5_x_std = [10.257386974222081,0.30340744019610855,0.30963527713419137,0.30879571571895686,0.29400818010532864,0.31330262106171375,0.2896566999182755]
_30875491d5aca995fc6528dbd253c5b5_y_std = [0.34951082802821465]
_30875491d5aca995fc6528dbd253c5b5_y_mean = [1.2975999926944448]
_30875491d5aca995fc6528dbd253c5b5_x_mean = [1.4388888888888889,0.5499999999999999,0.5037037037037038,0.48148148148148145,0.4972222222222222,0.5185185185185185,0.4759259259259259]
_5e86269a14fbd946491077d8aeeeb0d8_out = StreamingSources.createQueueStream(ssc, "/Users/jkidd/Projects/PSSAppliedResearch/emr-data-analytics/studio/../output/7in_1out_linear_csv.csv")
_fcd4a05c95166860df0d197fe8b8307d_out = _5e86269a14fbd946491077d8aeeeb0d8_out.map(lambda x: Filters.dictionaryToArray(x, "IN7OUT1MODEL/TAG1.CV,IN7OUT1MODEL/TAG2.CV,IN7OUT1MODEL/TAG3.CV,IN7OUT1MODEL/TAG4.CV,IN7OUT1MODEL/TAG5.CV,IN7OUT1MODEL/TAG7.CV,IN7OUT1MODEL/TAG6.CV"))
_30875491d5aca995fc6528dbd253c5b5_out = _fcd4a05c95166860df0d197fe8b8307d_out.map(lambda x: Transformations.normalize(x, _30875491d5aca995fc6528dbd253c5b5_x_mean, _30875491d5aca995fc6528dbd253c5b5_x_std)).map(lambda x: Transformations.dotProduct(x, _30875491d5aca995fc6528dbd253c5b5_model)).map(lambda x: Transformations.deNormalize(x, _30875491d5aca995fc6528dbd253c5b5_y_mean, _30875491d5aca995fc6528dbd253c5b5_y_std))
# _30875491d5aca995fc6528dbd253c5b5_out.foreachRDD(lambda rdd: output(rdd))
_30875491d5aca995fc6528dbd253c5b5_out.pprint()

ssc.start()
ssc.awaitTermination()

