
package emr.analytics.spark.algorithms

object Utilities {

  def columns(data: Any, features:String):Array[Double] = data match{
    case data:Map[String, Map[String, String]] => features.split(",").map(f => data.get(f).get("Val").toDouble)
  }

  def dotProduct(x: Array[Double], y:Array[Double]):Double = {
    x.zip(y).map(entry => entry._1*entry._2).sum
  }
}
