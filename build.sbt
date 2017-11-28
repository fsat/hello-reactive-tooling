organization in ThisBuild := "com.lightbend"
version in ThisBuild := "0.0.1"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val `hello-reactive-tooling` = (project in file("."))
  .aggregate(frontend)

lazy val frontend = (project in file("frontend"))
  .enablePlugins(PlayScala, SbtReactiveAppPlugin)
  .settings(
    reactiveLibVersion := "0.1.0-SNAPSHOT",

    // SBT Reactive TODO

    // - Play module auto-enable
    enablePlayHttpBinding := true,

    // - Setting namespace in ThisBuild from top level project name?
    // - Setting namespace should set the docker repo too?
    namespace := Some("hello"),
    dockerRepository := namespace.value,

    // - We need to configure allowed host filter to allow access from within container - HOW?

    // ------

    // This is required to configure Play's application loader
    libraryDependencies += guice
  )
