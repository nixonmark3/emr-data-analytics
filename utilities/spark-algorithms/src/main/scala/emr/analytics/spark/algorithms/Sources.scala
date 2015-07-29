package emr.analytics.spark.algorithms

import java.util.Calendar

import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.receiver.Receiver

import scala.collection.mutable.ListBuffer
import scala.util.parsing.json._

object Sources {

  def PollingStream(ssc: StreamingContext, url: String, sleep: String): DStream[Array[(String, Double)]] = {

    ssc.receiverStream[Array[(String, Double)]](new PollingSource(url, sleep.toInt))
  }

  def PiPollingStream(ssc: StreamingContext, host: String, port: String, sleep: String, query: String): DStream[Array[(String, Double)]] = {

    val columns = Utilities.extractQueryColumns(query)
    ssc.receiverStream[Array[(String, Double)]](new PiPollingSource(host, port, sleep.toInt, columns))
  }
}

class PollingSource(val url: String, val sleep: Int) extends Receiver[Array[(String, Double)]](StorageLevel.MEMORY_ONLY) with Logging {

  var isRunning = true

  def onStart(): Unit = {

    new Thread("Polling Source") {
      override def run() { poll() }
    }.start()
  }

  def onStop(): Unit = {
    isRunning = false
  }

  def poll(): Unit ={

    while(isRunning){

      try{
        val raw = scala.io.Source.fromURL(url).mkString

        val json = JSON.parseFull(raw).get

        val data = vectorize(json)

        store(data)
      }
      catch{
        case e:Exception => {
          // do nothing
        }
      }

      Thread.sleep(sleep)
    }
  }


  def vectorize(data: Any):Array[(String, Double)] = data match{

    case data:Map[String, Map[String, String]] => data.map(d => (d._1, d._2.get("Val").get.toDouble)).toArray
  }
}

class PiPollingSource(val host: String, port: String, val sleep: Int, val columns: Array[String]) extends Receiver[Array[(String, Double)]](StorageLevel.MEMORY_ONLY) with Logging {

  var isRunning = true

  def onStart(): Unit = {

    new Thread("Pi Polling Source") {
      override def run() { poll() }
    }.start()
  }

  def onStop(): Unit = {
    isRunning = false
  }

  def poll(): Unit ={

    while(isRunning){

      try{

        val end = Calendar.getInstance().getTimeInMillis()
        var start = end - 60000

        val data = new ListBuffer[(String, Double)]
        columns.foreach(col => {

          val endpoint = f"$host%s:$port%s/$start%d/$end%d?tag=$col%s"
          val raw = scala.io.Source.fromURL(endpoint).mkString
          val json:Map[String,Any] = JSON.parseFull(raw).get.asInstanceOf[Map[String, Any]]
          val colData:List[List[Double]] = json.get("data").get.asInstanceOf[List[List[Double]]]

          data += ((col, colData.tail.head(1)))
        })

        store(data.toArray)
      }
      catch{
        case e:Exception => {
          // do nothing
        }
      }

      Thread.sleep(sleep)
    }
  }
}