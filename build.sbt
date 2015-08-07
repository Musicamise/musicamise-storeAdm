name := """musicamise-store"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  filters,
  "org.mongodb" % "mongo-java-driver" % "2.13.0",
  "org.springframework" % "spring-core" % "4.1.5.RELEASE",
  "org.springframework" % "spring-context" % "4.1.5.RELEASE",
  "org.springframework.data" % "spring-data-mongodb" % "1.6.2.RELEASE",
  "org.apache.poi" % "poi-ooxml" % "3.10-FINAL",
  "javax.mail" % "mail" % "1.4.7",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.amazonaws" % "aws-java-sdk" % "1.3.11",
  "com.newrelic.agent.java" % "newrelic-api" % "3.15.0"
)
