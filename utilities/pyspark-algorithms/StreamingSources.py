import json
from pyspark.streaming.kafka import KafkaUtils

# deserialize the json string to a dictionary with
def jsonDeserializer(data):
    result = {}
    raw = json.loads(data)
    for val in raw["values"]:
        result[val["key"]] = val["value"]
    return result

#
def kafkaStream(ssc, topic, broker):
    return KafkaUtils.createDirectStream(ssc, [topic], {"metadata.broker.list": broker}, valueDecoder=jsonDeserializer)\
        .map(lambda x: x[1])

