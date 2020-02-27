import sbt._
import Keys._

val scioVersion = "0.8.1"
val beamVersion = "2.18.0"
val scalaMacrosVersion = "2.1.1"
val Http4sVersion = "0.20.17"
val CirceVersion = "0.11.1"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"
val PureConfigVersion = "0.12.2"
val DoobieVersion = "0.7.0"
val H2Version = "1.4.200"
val FlywayVersion = "6.1.3"

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "dj",
  // Semantic versioning http://semver.org/
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.10",
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-encoding",
    "UTF-8",
    "-language:higherKinds",
    "-language:postfixOps",
    "-feature",
    "-Ypartial-unification",
    "-Ywarn-unused:imports",
    "-Xfatal-warnings"
  ),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val paradiseDependency =
  "org.scalamacros" % "paradise" % scalaMacrosVersion cross CrossVersion.full
lazy val macroSettings = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin(paradiseDependency)
)

lazy val root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "dj",
    description := "dj",
    publish / skip := true,
    run / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-core" % scioVersion,
      "com.spotify" %% "scio-test" % scioVersion % Test,
      "org.apache.beam" % "beam-runners-direct-java" % beamVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-h2" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "org.flywaydb" % "flyway-core" % FlywayVersion,
      "org.specs2" %% "specs2-core" % Specs2Version % "test",
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")
  )
  .enablePlugins(PackPlugin)

lazy val repl: Project = project
  .in(file(".repl"))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "repl",
    description := "Scio REPL for dj",
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-repl" % scioVersion
    ),
    Compile / mainClass := Some("com.spotify.scio.repl.ScioShell"),
    publish / skip := true
  )
  .dependsOn(root)
