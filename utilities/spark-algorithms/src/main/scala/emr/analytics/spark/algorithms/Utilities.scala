
package emr.analytics.spark.algorithms

import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.kafka.KafkaUtils

object Utilities {

  def columns(data: Any, features:String):Array[Double] = data match{
    case data:Map[String, Map[String, String]] => features.split(",").map(f => data.get(f).get("Val").toDouble)
  }

  def dotProduct(x: Array[Double], y:Array[Double]):Double = {
    x.zip(y).map(entry => entry._1*entry._2).sum
  }

  def kafkaStream(ssc: StreamingContext,
                  zkQuorum: String,
                  groupId: String,
                  topics: String): ReceiverInputDStream[(String, String)] = {

    val topicsMap = topics.split(",").map(t => (t,1)).toMap
    KafkaUtils.createStream(ssc, zkQuorum, groupId, topicsMap)
  }
}
