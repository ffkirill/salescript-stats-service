name := "salescript-stat-service"
organization := "com.salescript"
version := "0.0.1"
scalaVersion := "2.11.8"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",

  "com.typesafe.slick" %% "slick" % "3.1.1",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.flywaydb" % "flyway-core" % "3.2.1",

  "org.slf4j" % "slf4j-nop" % "1.6.4",

  "com.zaxxer" % "HikariCP" % "2.4.5"
)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)