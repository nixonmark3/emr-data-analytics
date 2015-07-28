
package emr.analytics.spark.algorithms

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils

import scala.util.parsing.json.JSON

object Utilities {

  def columns(data:Any, features: String):Any = data match {

    case data:DStream[Array[(String, Double)]] =>
      val featuresSet = features.split(",")
      data.map(d => featuresSet.flatMap(x => d.filter(c => c._1.equals(x)).map(c => c._2)).to[Array])
    case data:RDD[Array[(String, Double)]] =>
      val featuresSet = features.split(",")
      data.map(d => featuresSet.flatMap(x => d.filter(c => c._1.equals(x)).map(c => c._2)).to[Array])
    case data:Array[(String, Double)] =>
      val featuresSet = features.split(",")
      featuresSet.flatMap(x => data.filter(c => c._1.equals(x)).map(c => c._2)).to[Array]
  }

  def predict(data:Any, model:Array[Double], xMean:Array[Double], xStdev:Array[Double], yMean:Array[Double], yStdev:Array[Double]):Any = {

    val x = normalize(data, xMean, xStdev)
    val y = dotProduct(x, model)
    deNormalize(y, yMean(0), yStdev(0))
  }

  def dotProduct(data:Any, y:Array[Double]):Any = data match {

    case data:DStream[Array[Double]] =>
      data.map(x => x.zip(y).map(entry => entry._1*entry._2).sum)
    case data:RDD[Array[Double]] =>
      data.map(x => x.zip(y).map(entry => entry._1*entry._2).sum)
    case data:Array[Double] =>
      data.zip(y).map(entry => entry._1*entry._2).sum
  }

  def normalize(data:Any, mean:Array[Double], stdev:Array[Double]):Any = data match {

    case data:DStream[Array[Double]] =>
      data.map(x => x.zip(mean).map(entry => entry._1-entry._2).zip(stdev).map(entry => entry._1 / entry._2))
    case data:RDD[Array[Double]] =>
      data.map(x => x.zip(mean).map(entry => entry._1-entry._2).zip(stdev).map(entry => entry._1 / entry._2))
    case data:Array[Double] =>
      data.zip(mean).map(entry => entry._1 - entry._2).zip(stdev).map(entry => entry._1 / entry._2)
  }

  def deNormalize(data:Any, mean:Double, stdev:Double):Any = data match {

    case data:DStream[Double] =>
      data.map(x => x * stdev + mean)
    case data:RDD[Double] =>
      data.map(x => x * stdev + mean)
    case data:Double =>
      data * stdev + mean
  }

  def fillNa(data:Any):Any = { data }

  def kafkaStream(ssc: StreamingContext,
                  zkQuorum: String,
                  groupId: String,
                  topics: String): ReceiverInputDStream[(String, String)] = {

    val topicsMap = topics.split(",").map(t => (t,1)).toMap
    KafkaUtils.createStream(ssc, zkQuorum, groupId, topicsMap)
  }

  def extractQueryColumns(query: String):Array[String] = {

    val raw:Option[Any] = JSON.parseFull(query)
    val json:Map[String,Any] = raw.get.asInstanceOf[Map[String, Any]]
    val columns:List[Map[String,String]] = json.get("columns").get.asInstanceOf[List[Map[String,String]]]

    columns.map(col => col.get("tag").get).toArray
  }
}
