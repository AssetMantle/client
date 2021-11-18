logLevel := Level.Warn

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.7")
//addSbtPlugin("play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.8.1"))

addSbtPlugin("io.gatling" % "gatling-sbt" % "3.0.0")
libraryDependencies += "com.spotify" % "docker-client" % "8.9.0"
