name := "spark-algorithms"

organization := "emr.analytics"

version := "1.0"

scalaVersion := "2.10.5"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq("emr.analytics.models" % "analytics-models" % "1.0-SNAPSHOT",
  "org.apache.httpcomponents" % "httpclient" % "4.4.1",
  "org.apache.spark" %% "spark-core" % "1.3.1",
  "org.apache.spark" %% "spark-streaming" % "1.3.1",
  "org.apache.spark" %% "spark-streaming-kafka" % "1.3.1")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

