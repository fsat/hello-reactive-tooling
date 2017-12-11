organization in ThisBuild := "com.lightbend"
version in ThisBuild := "0.0.1"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

lazy val `hello-reactive-tooling` = (project in file("."))
  .aggregate(
    frontend,
    `simple-api`,
    `simple-impl`,
    `clustered-api`,
    `clustered-impl`
  )

lazy val frontend = (project in file("frontend"))
  .enablePlugins(PlayScala, LagomPlay, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      guice, // This is required to configure Play's application loader
      ws
    )
  )

lazy val `simple-api` = (project in file("simple-api"))
  .settings(
    libraryDependencies += lagomScaladslApi
  )

lazy val `simple-impl` = (project in file("simple-impl"))
  .enablePlugins(LagomScala, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies += guice // This is required to configure Play's application loader
  )
  .dependsOn(`simple-api`)

lazy val `clustered-api` = (project in file("clustered-api"))
  .settings(
    libraryDependencies += lagomScaladslApi
  )

lazy val `clustered-impl` = (project in file("clustered-impl"))
  .enablePlugins(LagomScala, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= {
      Seq(
        lagomScaladslCluster,
        "com.lightbend.rp" %% "reactive-lib-service-discovery-lagom14-scala" % reactiveLibVersion.value, // FIXME: this should come from SBT Reactive App,
        "com.typesafe.akka" %% "akka-distributed-data" % "2.5.6",
        "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
      )
    }
  )
  .dependsOn(`clustered-api`, `simple-api`)
