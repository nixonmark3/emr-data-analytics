import sys, getopt, traceback
import json
import numpy as np
import pandas as pd

from py4j.java_gateway import java_import, JavaGateway, GatewayClient
from py4j.protocol import Py4JJavaError

# reference the py4J client
client = GatewayClient(port=int(sys.argv[1]))
gateway = JavaGateway(client, auto_convert = True)

# reference interpreter
interpreter = gateway.entry_point
from pyspark.conf import SparkConf
from pyspark.context import SparkContext
from pyspark.rdd import RDD
from pyspark.files import SparkFiles
from pyspark.storagelevel import StorageLevel
from pyspark.accumulators import Accumulator, AccumulatorParam
from pyspark.broadcast import Broadcast
from pyspark.serializers import MarshalSerializer, PickleSerializer
from pyspark.sql import SQLContext, Row

java_import(gateway.jvm, "org.apache.spark.SparkEnv")
java_import(gateway.jvm, "org.apache.spark.SparkConf")
java_import(gateway.jvm, "org.apache.spark.api.java.*")
java_import(gateway.jvm, "org.apache.spark.api.python.*")
java_import(gateway.jvm, "org.apache.spark.mllib.api.python.*")
java_import(gateway.jvm, "org.apache.spark.sql.*")
java_import(gateway.jvm, "org.apache.spark.sql.hive.*")

java_import(gateway.jvm, "scala.Tuple2")

jsc = interpreter.getSparkContext()
jconf = interpreter.getSparkConf()
jsql = interpreter.getSQLContext()
conf = SparkConf(_jvm = gateway.jvm, _jconf = jconf)
sc = SparkContext(jsc=jsc, gateway=gateway, conf=conf)
sqlContext = SQLContext(sc, jsql)

from pyspark.streaming import StreamingContext
from pyspark.streaming.dstream import DStream
from pyspark.streaming.util import TransformFunction, TransformFunctionSerializer

java_import(gateway.jvm, "org.apache.spark.streaming.*")
java_import(gateway.jvm, "org.apache.spark.streaming.api.java.*")
java_import(gateway.jvm, "org.apache.spark.streaming.api.python.*")
java_import(gateway.jvm, "org.apache.spark.streaming.kafka.*")

jssc = interpreter.getStreamingContext()
ssc = StreamingContext(sc, None, jssc)
StreamingContext._ensure_initialized()
# notify the client that the python script has been initialized
interpreter.onPythonScriptInitialized()

while True:
    request = interpreter.getStatements()
    try:
        statements = request.getStatements().split("\n")
        source = None

        for statement in statements:
            if statement == None or len(statement.strip()) == 0:
                continue

            # skip comment
            if statement.strip().startswith("#"):
                continue

            if source:
                source += "\n" + statement
            else:
                source = statement

        if source:
            compiledCode = compile(source, "<string>", "exec")
            eval(compiledCode)

        interpreter.setStatementsComplete()

    except Py4JJavaError:
        interpreter.setStatementsFailed(traceback.format_exc())
        # interpreter.setStatementsFailed(traceback.format_exc() + str(sys.exc_info()))

    except:
        interpreter.setStatementsFailed(traceback.format_exc())