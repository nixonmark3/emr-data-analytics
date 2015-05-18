package emr.analytics.spark.algorithms

import java.util

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

object Requests {

  def postOpcValue(url: String, tag: String, value: Any): Boolean ={

    val post = new HttpPost(url)
    val payload = "%s,%s".format(tag, value.toString())

    post.setEntity(new StringEntity(payload))

    val client = HttpClientBuilder.create().build()
    client.execute(post)

    true
  }

}
