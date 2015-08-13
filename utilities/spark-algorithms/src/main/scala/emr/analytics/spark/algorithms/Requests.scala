package emr.analytics.spark.algorithms

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

object Requests {

  def postValue(postType: String, url: String, tag: String, value: Any): Any = value match {

    case value:DStream[Any] => {

      value.foreachRDD(rdd =>
        if (!rdd.isEmpty()) {
          rdd.foreach(item => {

            if (postType == "PI") {

              val x = item.asInstanceOf[Array[Double]]
              val y = x.head
              postPi(url, tag, y)
            }
            else {

              postOpc(url, tag, item.asInstanceOf[Double])
            }
          })
        }
      )

      value
    }

    case value:RDD[Double] => {

      if (!value.isEmpty()) {
        value.foreach(item => {
          postOpc(url, tag, item)
        })
      }

      value
    }

    case value:Double => {
      postOpc(url, tag, value)

      value
    }
  }

  def postOpc(url: String, tag: String, value: Double): Boolean = {
    val post = new HttpPost(url)

    val payload = "%s,%s".format(tag, value.toString)

    post.setEntity(new StringEntity(payload))

    val client = HttpClientBuilder.create().build()
    client.execute(post)

    true
  }

  def postPi(url: String, tag: String, value: Double): Boolean = {

    val post = new HttpPost(url)

    val payload = "{'items': [{'tag':'%s', 'ts':0, 'type':'F', 'value':'%s'}]}".format(tag, value.toString)

    post.setEntity(new StringEntity(payload))

    val client = HttpClientBuilder.create().build()
    client.execute(post)

    true
  }
}
