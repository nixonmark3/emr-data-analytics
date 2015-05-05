"""
 Gets opc json from rest api request and writes to kafka topic

 Usage: opcproducer.py <broker> <topic> <url>
 To run locally, you must start kafka and create a topic first
 Example:
    $ python opcproducer.py localhost:9092 runtime http://192.168.17.129:8000/jsondata/InferredCalc1
"""

from __future__ import print_function

__author__ = 'jkidd'

import sys
import requests
from kafka import SimpleProducer, KafkaClient
import time

if __name__ == "__main__":

    # verify the input arguments
    if len(sys.argv) != 4:
        print("Usage: opcproducer.py <broker> <topic> <url>", file=sys.stderr)
        exit(-1)

    # reference the kafka broker, topic and url for the opc rest api
    broker, topic, url = sys.argv[1:]

    # create kafka client and simple producer
    kafka = KafkaClient(broker)
    producer = SimpleProducer(kafka)

    while True:
        # get
        response = requests.get(url)
        # send json to kafka topic
        producer.send_messages(topic, response.content)

        # sleep
        time.sleep(1)
