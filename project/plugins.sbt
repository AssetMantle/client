logLevel := Level.Warn

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.20")

addSbtPlugin("io.gatling" % "gatling-sbt" % "3.0.0")

// Fingerprint static assets (CSS/JS/fonts) at build time so URLs produced by
// @routes.Assets.versioned("...") become content-addressed. Pairs with
// Play's default long-TTL Cache-Control on versioned assets so a deploy
// auto-invalidates browser caches without manual ?version= bumps.
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")
