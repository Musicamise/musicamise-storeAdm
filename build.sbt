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
  "org.springframework" % "spring-core" % "4.1.6.RELEASE",
  "org.springframework" % "spring-context" % "4.1.6.RELEASE",
  "org.springframework.data" % "spring-data-mongodb" % "1.7.2.RELEASE",
  "org.apache.poi" % "poi-ooxml" % "3.10-FINAL",
  "javax.mail" % "mail" % "1.4.7",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.amazonaws" % "aws-java-sdk" % "1.3.11",
  "com.typesafe.play" % "play-mailer_2.10" % "2.4.1",
  "com.newrelic.agent.java" % "newrelic-api" % "3.15.0",
  "com.tinify" % "tinify" % "1.1.1"
)
