
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
