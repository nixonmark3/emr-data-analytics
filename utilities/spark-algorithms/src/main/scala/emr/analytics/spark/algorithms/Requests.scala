package emr.analytics.spark.algorithms

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

object Requests {

  def postOpcValue(url: String, tag: String, value: Any): Boolean = value match {

    case value:DStream[Double] =>

      value.foreachRDD(rdd =>
        if (!rdd.isEmpty()){
          rdd.foreach(item =>
            postOpc(url, tag, item)
          )
        }
      )
      true

    case value:RDD[Double] =>

      if (!value.isEmpty()){
        value.foreach(item =>
          postOpc(url, tag, item)
        )
      }
      true

    case value:Double =>

      postOpc(url, tag, value)
  }

  def postOpc(url: String, tag: String, value: Double): Boolean = {
    val post = new HttpPost(url)
    val payload = "%s,%s".format(tag, value.toString)

    post.setEntity(new StringEntity(payload))

    val client = HttpClientBuilder.create().build()
    client.execute(post)

    true
  }
}
