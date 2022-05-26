name := "assetMantle"

maintainer := "admin@assetmantle.one"

version := "1.0"

lazy val GatlingTest = config("gatling") extend Test

GatlingTest / scalaSource  := baseDirectory.value / "gatling/simulation"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(GatlingPlugin)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.12.14"

libraryDependencies ++= Seq(ws, specs2 % Test, guice, caffeine)

libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"

libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"

libraryDependencies += "org.postgresql" % "postgresql" % "42.3.4"

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "8.0.1"

libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"

libraryDependencies += "com.twilio.sdk" % "twilio" % "8.29.1"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.1.1" % "test"

libraryDependencies += "io.gatling" % "gatling-test-framework" % "3.1.1" % "test"

Test / unmanagedResourceDirectories  += (baseDirectory.value / "target/web/public/test")

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.8"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.8"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.8"

libraryDependencies += "commons-codec" % "commons-codec" % "20041127.091804"

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-ftp" % "3.0.4"

libraryDependencies += "org.bouncycastle" % "bcpg-jdk15on" % "1.70"

libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" artifacts(Artifact("javax.ws.rs-api", "jar", "jar"))

libraryDependencies += "com.docusign" % "docusign-esign-java" % "3.18.0"

libraryDependencies += "com.sun.jersey" % "jersey-core" % "1.19.4"

libraryDependencies += "org.glassfish.jersey.core" % "jersey-common" % "2.35"

libraryDependencies += "org.scodec" %% "scodec-bits" % "1.1.31"

libraryDependencies += "org.scorexfoundation" %% "scrypto" % "2.2.1"

libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.16.1"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.3"
