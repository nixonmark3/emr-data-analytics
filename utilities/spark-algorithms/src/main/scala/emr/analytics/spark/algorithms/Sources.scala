package emr.analytics.spark.algorithms

import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver

object Sources {

  def PollingStream(ssc: StreamingContext, url: String, sleep: String): ReceiverInputDStream[(String, String)] = {

    ssc.receiverStream[(String, String)](new PollingSource(url, sleep.toInt))
  }
}

class PollingSource(val url: String, val sleep: Int) extends Receiver[(String, String)](StorageLevel.MEMORY_ONLY) with Logging {

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
        val data = scala.io.Source.fromURL(url).mkString
        store(("", data))
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