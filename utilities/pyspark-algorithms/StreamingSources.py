import json
from pyspark.streaming.kafka import KafkaUtils

# deserialize the json string to a dictionary
def jsonDeserializer(data):
    result = {}
    raw = json.loads(data)
    for val in raw["values"]:
        result[val["key"]] = val["value"]
    return result

# try to cast the specified value as a number
def castAsNumber(value):
    try:
        num = float(value)
        return num
    except ValueError:
        return value

# using the specified streaming context, topic, and kafka broker - create a direct stream
def kafkaStream(ssc, topic, broker):
    return KafkaUtils.createDirectStream(ssc, [topic], {"metadata.broker.list": broker}, valueDecoder=jsonDeserializer)\
        .map(lambda x: x[1])

# using the specified path - create a queue stream
def createQueueStream(ssc, path):

    # read the file contents into an array
    with open(path, 'rU') as f:
        data = [line for line in f]

    # capture the header row
    header = data[0].split(',')

    # build a queue of rdd's
    queue = []
    for i in range(1, len(data)):
        queue += [ssc.sparkContext.parallelize([{header[index].strip(): castAsNumber(x.strip()) for index, x in enumerate(data[i].split(','))}])]

    # create a queue stream
    return ssc.queueStream(queue)