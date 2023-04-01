name := "assetMantle"

maintainer := "admin@assetmantle.one"

version := "1.0"

lazy val GatlingTest = config("gatling") extend Test

GatlingTest / scalaSource := baseDirectory.value / "gatling/simulation"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(GatlingPlugin)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Maven Central Server" at "https://repo1.maven.org/maven2"

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(ws, specs2 % Test, guice, caffeine)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
  "org.postgresql" % "postgresql" % "42.5.4"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-mailer" % "8.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"
)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.9.0" % "test",
  "io.gatling" % "gatling-test-framework" % "3.9.0" % "test"
)

Test / unmanagedResourceDirectories += (baseDirectory.value / "target/web/public/test")

libraryDependencies ++= Seq(
  "com.sksamuel.scrimage" % "scrimage-core" % "4.0.33",
  "com.sksamuel.scrimage" %% "scrimage-scala" % "4.0.33"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0",
  "com.typesafe.akka" %% "akka-serialization-jackson" % "2.8.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.8.0",
  "com.lightbend.akka" %% "akka-stream-alpakka-ftp" % "5.0.0"
)

libraryDependencies ++= Seq(
  "org.bouncycastle" % "bcpg-jdk15on" % "1.70",
  "org.scodec" %% "scodec-bits" % "1.1.37",
  "org.scorexfoundation" %% "scrypto" % "2.3.0",
  "org.bitcoinj" % "bitcoinj-core" % "0.16.2"
)

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2"

libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.22.2"

libraryDependencies += "com.typesafe" % "config" % "1.4.2"