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

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Maven Central Server" at "https://repo1.maven.org/maven2"

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(ws, specs2 % Test, guice, caffeine)

libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.1.0"

libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0"

libraryDependencies += "org.postgresql" % "postgresql" % "42.5.1"

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "8.0.1"

libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"

libraryDependencies += "com.twilio.sdk" % "twilio" % "9.2.0"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.9.0" % "test"

libraryDependencies += "io.gatling" % "gatling-test-framework" % "3.9.0" % "test"

Test / unmanagedResourceDirectories  += (baseDirectory.value / "target/web/public/test")

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-core" % "4.0.32"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-scala" % "4.0.32"

libraryDependencies += "commons-codec" % "commons-codec" % "20041127.091804"

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-ftp" % "5.0.0"

libraryDependencies += "org.bouncycastle" % "bcpg-jdk15on" % "1.70"

libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" artifacts Artifact("javax.ws.rs-api", "jar", "jar")

libraryDependencies += "com.docusign" % "docusign-esign-java" % "3.22.0"

libraryDependencies += "com.sun.jersey" % "jersey-core" % "1.19.4"

libraryDependencies += "org.glassfish.jersey.core" % "jersey-common" % "3.1.0"

libraryDependencies += "org.scodec" %% "scodec-bits" % "1.1.34"

libraryDependencies += "org.scorexfoundation" %% "scrypto" % "2.2.1"

libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.16.2"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.1"
