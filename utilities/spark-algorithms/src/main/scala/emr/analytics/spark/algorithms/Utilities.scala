
package emr.analytics.spark.algorithms

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils

object Utilities {

  /*def columns(stream: DStream[Array[(String, Double)]], features: String):DStream[Array[Double]] = {

    val featuresSet = features.split(",")
    stream.map(data => data.filter(x => featuresSet.contains(x._1)).map(x => x._2))
  }

  def dotProduct(x: Array[Double], y:Array[Double]):Double = {
    x.zip(y).map(entry => entry._1*entry._2).sum
  }*/

  def columns(data:Any, features: String):Any = data match {

    case data:DStream[Array[(String, Double)]] =>
      val featuresSet = features.split(",")
      data.map(d => d.filter(x => featuresSet.contains(x._1)).map(x => x._2))
    case data:RDD[Array[(String, Double)]] =>
      val featuresSet = features.split(",")
      data.map(d => d.filter(x => featuresSet.contains(x._1)).map(x => x._2))
    case data:Array[(String, Double)] =>
      val featuresSet = features.split(",")
      data.filter(x => featuresSet.contains(x._1)).map(x => x._2)
  }

  def dotProduct(data:Any, y:Array[Double]):Any = data match {

    case data:DStream[Array[Double]] =>
      data.map(x => x.zip(y).map(entry => entry._1*entry._2).sum)
    case data:RDD[Array[Double]] =>
      data.map(x => x.zip(y).map(entry => entry._1*entry._2).sum)
    case data:Array[Double] =>
      data.zip(y).map(entry => entry._1*entry._2).sum
  }

  def fillNa(data:Any):Any = { data }

  def kafkaStream(ssc: StreamingContext,
                  zkQuorum: String,
                  groupId: String,
                  topics: String): ReceiverInputDStream[(String, String)] = {

    val topicsMap = topics.split(",").map(t => (t,1)).toMap
    KafkaUtils.createStream(ssc, zkQuorum, groupId, topicsMap)
  }
}
