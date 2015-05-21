package emr.analytics.spark.algorithms

import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.receiver.Receiver

import scala.util.parsing.json._

object Sources {

  def PollingStream(ssc: StreamingContext, url: String, sleep: String): DStream[Array[(String, Double)]] = {

    ssc.receiverStream[Array[(String, Double)]](new PollingSource(url, sleep.toInt))
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