name := "SparkTestHarness"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.3.1",
  "org.apache.spark" %% "spark-streaming" % "1.3.1",
  "org.apache.httpcomponents" % "httpclient" % "4.4.1",
  "emr.analytics" %% "spark-algorithms" % "1.0")

resolvers ++= Seq(Resolver.mavenLocal,
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Spray Repository" at "http://repo.spray.cc/")
