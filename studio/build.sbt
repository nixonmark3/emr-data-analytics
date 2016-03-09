name := """studio"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.10.5"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "emr.analytics.models" % "analytics-models" % "1.0-SNAPSHOT",
  "emr.analytics.compiler" % "analytics-compiler" % "1.0-SNAPSHOT",
  "emr.analytics.database" % "analytics-database" % "1.0-SNAPSHOT",
  "com.typesafe.akka" %% "akka-remote" % "2.3.9",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.apache.httpcomponents" % "httpclient" % "4.5",
  "org.webjars" % "bootstrap" % "3.3.2",
  "org.webjars" % "angularjs" % "1.3.14",
  "org.webjars" % "angular-ui-bootstrap" % "0.12.0",
  "org.webjars.bower" % "angular-ui-codemirror" % "0.3.0",
  "org.webjars.bower" % "d3" % "3.5.5",
  "org.webjars" % "font-awesome" % "4.4.0",
  "org.webjars" % "jquery" % "2.1.3"
)
