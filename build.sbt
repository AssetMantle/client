import _root_.io.gatling.sbt.GatlingPlugin
import _root_.sbt.Keys._

name := "commitCentral"

maintainer := "deepanshu@cosmiclabs.co"

version := "1.0"

lazy val GatlingTest = config("gatling") extend Test

scalaSource in GatlingTest := baseDirectory.value / "gatling/simulation"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .enablePlugins(GatlingPlugin)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(ehcache, ws, specs2 % Test, guice)

libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.1"

libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1"

libraryDependencies += "org.postgresql" % "postgresql" % "42.1.4"

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.1"

libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"

libraryDependencies += "com.twilio.sdk" % "twilio" % "7.15.5"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.1.1" % "test"

libraryDependencies += "io.gatling" % "gatling-test-framework" % "3.1.1" % "test"

unmanagedResourceDirectories in Test += (baseDirectory.value / "target/web/public/test")

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.8"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.8"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.8"

libraryDependencies += "commons-codec" % "commons-codec" % "1.11"

      