import _root_.io.gatling.sbt.GatlingPlugin
import _root_.sbt.Keys._

name := "commitCentral"

maintainer := "deepanshu@persistence.one"

version := "1.0"

lazy val GatlingTest = config("gatling") extend Test

scalaSource in GatlingTest := baseDirectory.value / "gatling/simulation"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .enablePlugins(GatlingPlugin)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.12.11"

libraryDependencies ++= Seq(ehcache, ws, specs2 % Test, guice)

libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"

libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.8"

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "8.0.0"

libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.0"

libraryDependencies += "com.twilio.sdk" % "twilio" % "7.49.0"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.1.1" % "test"

libraryDependencies += "io.gatling" % "gatling-test-framework" % "3.1.1" % "test"

unmanagedResourceDirectories in Test += (baseDirectory.value / "target/web/public/test")

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.8"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.8"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.8"

libraryDependencies += "commons-codec" % "commons-codec" % "1.14"

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-ftp" % "1.1.2"

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "2.0.1"

libraryDependencies += "org.bouncycastle" % "bcpg-jdk15on" % "1.65"

libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts(Artifact("javax.ws.rs-api", "jar", "jar"))

libraryDependencies += "com.docusign" % "docusign-esign-java" % "3.5.0-RC1"

libraryDependencies += "com.sun.jersey" % "jersey-core" % "1.19"

libraryDependencies += "org.glassfish.jersey.core" % "jersey-common" % "2.2"
