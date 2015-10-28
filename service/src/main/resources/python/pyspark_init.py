
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

# collection of methods to pass data through the gateway
class DataGateway(object):

    # sends a dataframe's schema and data to the interpreter
    def collect(self, dataFrame):
        interpreter.collect(dataFrame.schema.json(), dataFrame.columns, dataFrame.collect())

    # sends a dataframe's schema and describe statistics to the interpreter
    def describe(self, dataFrame):
        stats = dataFrame.describe()
        interpreter.describe(dataFrame.schema.json(), stats.columns, stats.collect())

dataGateway = DataGateway()
