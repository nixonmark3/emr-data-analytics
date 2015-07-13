import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

import org.apache.log4j.PropertyConfigurator

/*
** Spark submit command:
** $SPARK_HOME/bin/spark-submit --class "SparkTestHarness" --master local[4] --driver-memory 2g --jars target/scala-2.10/sparkstreaming_sparktestharness_2.10-1.0.jar
*/

object SparkTestHarness {

  def main(args: Array[String]): Unit ={

    PropertyConfigurator.configure("log4j.properties")

    val sparkConf = new SparkConf()
      .setAppName("SparkTestHarness")
      .setMaster("local[4]")

    val ssc = new StreamingContext(sparkConf, Seconds(1))

    /*
    ** Begin Spark
     */

    import emr.analytics.spark.algorithms.Sources
    import emr.analytics.spark.algorithms.Utilities
    import emr.analytics.spark.algorithms.Requests

    val _06d52e8abd2da4234d150a2e08f9f3cb_x_std = Array(10.257386974222081,0.30340744019610855,0.30963527713419137,0.30879571571895686,0.29400818010532864,0.2896566999182755,0.31330262106171375)
    val _06d52e8abd2da4234d150a2e08f9f3cb_y_mean = Array(1.297452481564815)
    val _06d52e8abd2da4234d150a2e08f9f3cb_model = Array(-0.009021405423909428,0.17079278906854212,0.29714844436968274,0.42846768472960584,0.44343526780300746,0.6313075123508008,0.6782360212033129)
    val _06d52e8abd2da4234d150a2e08f9f3cb_x_mean = Array(1.4388888888888889,0.5499999999999999,0.5037037037037038,0.48148148148148145,0.4972222222222222,0.4759259259259259,0.5185185185185185)
    val _06d52e8abd2da4234d150a2e08f9f3cb_y_std = Array(0.34948128022023056)

    val appName = ssc.sparkContext.appName

    val _0e729fcdbc0f1468686db2733c24e090_out = Sources.PollingStream(ssc, "http://172.16.167.131:8000/jsondata/7in1out", "1000")

    val _1f9f44b50c7984e5bbd4d1a7f36ee6f1_out = Utilities.columns(_0e729fcdbc0f1468686db2733c24e090_out, "IN7OUT1MODEL/TAG1.CV,IN7OUT1MODEL/TAG2.CV,IN7OUT1MODEL/TAG3.CV,IN7OUT1MODEL/TAG4.CV,IN7OUT1MODEL/TAG5.CV,IN7OUT1MODEL/TAG6.CV,IN7OUT1MODEL/TAG7.CV")

    val _06d52e8abd2da4234d150a2e08f9f3cb_out = Utilities.predict(_1f9f44b50c7984e5bbd4d1a7f36ee6f1_out, _06d52e8abd2da4234d150a2e08f9f3cb_model, _06d52e8abd2da4234d150a2e08f9f3cb_x_mean, _06d52e8abd2da4234d150a2e08f9f3cb_x_std, _06d52e8abd2da4234d150a2e08f9f3cb_y_mean, _06d52e8abd2da4234d150a2e08f9f3cb_y_std)

    val _64b42aa0a74e42f4bd81e15abf27d732 = Requests.postOpcValue("http://172.16.167.131:8000/updatedata/7in1out", "IN7OUT1MODEL/Y_PRED.CV", _06d52e8abd2da4234d150a2e08f9f3cb_out)

    /*
    ** End Spark
     */

    ssc.start()
    ssc.awaitTermination()
  }
}
