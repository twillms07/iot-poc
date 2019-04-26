name := "iot-poc"

version := "0.1"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.22"
lazy val akkaHttpVersion = "10.1.8"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % "1.21.0",
    "org.json4s" %% "json4s-jackson" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
