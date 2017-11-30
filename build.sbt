organization in ThisBuild := "com.lightbend"
version in ThisBuild := "0.0.1"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val `hello-reactive-tooling` = (project in file("."))
  .aggregate(frontend)

lazy val frontend = (project in file("frontend"))
  .enablePlugins(PlayScala, SbtReactiveAppPlugin)
  .settings(
    // This is required to configure Play's application loader
    libraryDependencies += guice
  )
