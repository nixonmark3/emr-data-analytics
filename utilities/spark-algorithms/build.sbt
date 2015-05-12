name := "spark-algorithms"

organization := "emr.analytics"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq("org.apache.httpcomponents" % "httpclient" % "4.4.1")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

