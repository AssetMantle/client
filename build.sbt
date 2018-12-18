name := "comdex"

version := "1.0"

lazy val `comdex` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(jdbc, ehcache, ws, specs2 % Test, guice)

libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"

libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1100-jdbc4"

unmanagedResourceDirectories in Test += (baseDirectory.value / "target/web/public/test")

      